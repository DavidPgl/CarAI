package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;
import s0564478.CarAI;
import s0564478.util.GLUtil;
import s0564478.util.Line;
import s0564478.util.Pair;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SteeringBehaviour {
    private static final float goalAngle = 1.2f;
    private static final float decelerateAngle = 47.6f;
    private static final float steerTime = 0.3f;

    private static final double collisionAvoidanceRadius = 50.0d;

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
            //ai.addDebugAction(() -> GLUtil.drawLine(info.getX(), info.getY(), boundings.getCenterX(), boundings.getCenterY(), Color.WHITE));
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


        Vector2f orth1 = new Vector2f(-polygonDirection.getY(), polygonDirection.getX());
        Vector2f orth2 = new Vector2f(polygonDirection.getY(), -polygonDirection.getX());
        if (!preventBeingStuck)
            lastObstacle.setSecond(Vector2f.angle(checkpointDirection, orth1) < Vector2f.angle(checkpointDirection, orth2));

        // Calculate distance to intersection
        Line line = new Line(info.getX(), info.getY(), info.getX() + polygonDirection.getX(), info.getY() + polygonDirection.getY());
        Point2D intersection = getClosestIntersection(lastObstacle.getFirst(), line);

        double distance = intersection.distance(info.getX(), info.getY());
        System.out.println(distance);

        return new Pair<>(lastObstacle.getSecond() ? orth1 : orth2, distance);
    }

    private Point2D.Double getClosestIntersection(Polygon polygon, Line line) {
        double closestDistance = Double.MAX_VALUE;
        Point2D.Double closestPoint = null;

        for (int i = 0; i < polygon.npoints; i++) {
            Line polygonLine = new Line(polygon.xpoints[i], polygon.ypoints[i], polygon.xpoints[i % polygon.npoints], polygon.ypoints[i % polygon.npoints]);
            Point2D.Double intersection = intersect(line, polygonLine);
            if (intersection == null)
                continue;

            double distance = Point.distance(intersection.x, intersection.y, info.getX(), info.getY());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = intersection;
            }
        }

        return closestPoint;
    }

    private Point2D.Double intersect(Line first, Line second) {
        // Parallel or on-top of each other
        if (first.getM() == second.getM())
            return null;

        double x = (first.getB() - second.getB()) / (second.getM() - first.getM());
        double y = first.getM() * x + first.getB();

        // Horizontal line -> Check only x
        if (second.getM() == 0) {
            if (valueBetween(x, second.getX1(), second.getX2())) {
                return new Point2D.Double(x, y);
            }
        }
        // Vertical line -> Check only y
        else if (Math.abs(second.getM()) == Double.MAX_VALUE) {
            x = second.getX1();
            y = first.getM() * x + first.getB();
            if (valueBetween(y, second.getY1(), second.getY2())) {
                return new Point2D.Double(x, y);
            }
        }
        // Check both
        else if (valueBetween(x, second.getX1(), second.getX2()) || valueBetween(y, second.getY1(), second.getY2())) {
            return new Point2D.Double(x, y);
        }

        return null;
    }

    private boolean valueBetween(double value, double a, double b) {
        return (value >= a && value <= b) || (value <= a && value >= b);
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
