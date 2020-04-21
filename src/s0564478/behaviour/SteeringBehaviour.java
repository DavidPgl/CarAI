package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;
import s0564478.CarAI;
import s0564478.util.GLUtil;
import s0564478.util.Pair;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class SteeringBehaviour {
    private static final float goalAngle = 1.2f;
    private static final float decelerateAngle = 47.6f;
    private static final float steerTime = 0.3f;

    private static final double collisionAvoidanceRadius = 30.0d;

    private Vector2f checkpointDirection = null;
    // Left: false | Right: true
    private final Pair<Polygon, Boolean> lastObstacle = new Pair<>(null, false);

    private final Info info;
    private final CarAI ai;

    // Debug stuff
    Vector2f avoidanceDirection = new Vector2f(0, 0);

    public SteeringBehaviour(Info info, CarAI ai) {
        this.info = info;
        this.ai = ai;
    }

    public float getSteering() {
        Point checkpoint = info.getCurrentCheckpoint();
        checkpointDirection = new Vector2f((float) checkpoint.getX() - info.getX(), (float) checkpoint.getY() - info.getY());

        float steering = getSteeringTo(checkpointDirection);
        Pair<Vector2f, Double> collisionAvoidance = getCollisionAvoidance();

        avoidanceDirection = collisionAvoidance == null ? new Vector2f(0, 0) : collisionAvoidance.getFirst();
        doDebugStuff();

        if (collisionAvoidance != null) {
            float avoidanceSteering = getSteeringTo(collisionAvoidance.getFirst());
            double weight = collisionAvoidance.getSecond() / collisionAvoidanceRadius;
            steering = (float) (steering * weight + avoidanceSteering * (1 - weight));
        }

        return steering;
    }

    private float getSteeringTo(Vector2f destinationDirection) {
        Vector2f carDirection = getCarDirection();
        // Get angle between current and destination direction
        double diffAngle = Vector2f.angle(carDirection, destinationDirection);
        diffAngle = Math.toDegrees(diffAngle);
        boolean angleIsNegative = angleIsNegative(carDirection, destinationDirection);

        // Calculate steering velocity
        if (diffAngle < goalAngle)
            return 0;
        else if (diffAngle < decelerateAngle) {
            double newVelocity = diffAngle / decelerateAngle * info.getMaxAbsoluteAngularVelocity() * (angleIsNegative ? -1 : 1);
            float newAcceleration = (float) ((newVelocity - info.getAngularVelocity()) / steerTime);
            return Math.max(Math.min(newAcceleration, info.getMaxAbsoluteAngularAcceleration()), -info.getMaxAbsoluteAngularAcceleration());
        } else
            return info.getMaxAbsoluteAngularAcceleration() * (angleIsNegative ? -1 : 1);
    }

    private boolean angleIsNegative(Vector2f from, Vector2f to) {
        return 0 >= from.getX() * to.getY() - from.getY() * to.getX();
    }

    private Vector2f getCarDirection() {
        float orientation = info.getOrientation();
        return new Vector2f((float) Math.cos(orientation), (float) Math.sin(orientation));
    }


    private Pair<Vector2f, Double> getCollisionAvoidance() {
        Polygon[] polygons = info.getTrack().getObstacles();

        Ellipse2D circle = new Ellipse2D.Double(
                info.getX() - collisionAvoidanceRadius,
                info.getY() - collisionAvoidanceRadius,
                collisionAvoidanceRadius * 2,
                collisionAvoidanceRadius * 2);

        Area circleArea = new Area(circle);


        // Get obstacle in reach
        Vector2f polygonDirection = null;
        boolean preventBeingStuck = false;
        double squareDistance = Double.MAX_VALUE;
        for (Polygon polygon : polygons) {
            Area polygonArea = new Area(polygon);
            polygonArea.intersect(circleArea);
            if (polygonArea.isEmpty())
                continue;
            Rectangle2D boundings = polygonArea.getBounds2D();
            Vector2f currentPolygonDirection = new Vector2f((float) boundings.getCenterX() - info.getX(), (float) boundings.getCenterY() - info.getY());

            if (currentPolygonDirection.lengthSquared() > squareDistance)
                continue;

            polygonDirection = currentPolygonDirection;
            squareDistance = polygonDirection.lengthSquared();
            // Prevent switching directions while in front of same polygon
            if (lastObstacle.getFirst() == polygon)
                preventBeingStuck = true;
            lastObstacle.setFirst(polygon);
        }

        if (polygonDirection == null)
            return null;

        System.out.println(polygonDirection.length());


        Vector2f orth1 = new Vector2f(-polygonDirection.getY(), polygonDirection.getX());
        Vector2f orth2 = new Vector2f(polygonDirection.getY(), -polygonDirection.getX());
        if (!preventBeingStuck)
            lastObstacle.setSecond(Vector2f.angle(checkpointDirection, orth1) < Vector2f.angle(checkpointDirection, orth2));

        return new Pair<>(lastObstacle.getSecond() ? orth1 : orth2, (double) polygonDirection.length());


    }

    private void doDebugStuff() {
        ai.addDebugAction(() -> {
            Vector2f carPosition = new Vector2f(info.getX(), info.getY());
            GLUtil.drawLine(carPosition, checkpointDirection, Color.GREEN);
            GLUtil.drawLine(carPosition, avoidanceDirection, Color.RED);
            //GLUtil.drawLine(carPosition, new Vector2f((float) collisionAvoidanceRadius, (float) collisionAvoidanceRadius), Color.BLACK, false);
            //GLUtil.drawLine(carPosition, new Vector2f((float) collisionAvoidanceRadius, -(float) collisionAvoidanceRadius), Color.BLACK, false);
            //GLUtil.drawLine(carPosition, new Vector2f(-(float) collisionAvoidanceRadius, (float) collisionAvoidanceRadius), Color.BLACK, false);
            //GLUtil.drawLine(carPosition, new Vector2f(-(float) collisionAvoidanceRadius, -(float) collisionAvoidanceRadius), Color.BLACK, false);

        });
    }
}
