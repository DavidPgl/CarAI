package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;
import s0564478.CarAI;
import s0564478.util.GLUtil;
import s0564478.util.Line;
import s0564478.util.Pair;

import java.awt.*;

public class SteeringBehaviour {
    private static final float goalAngle = 1.2f;
    private static final float decelerateAngle = 47.6f;
    private static final float steerTime = 0.3f;

    private static final float collisionAvoidanceRadius = 50.0f;

    private final Info info;
    private final CarAI ai;

    Line closestObstacleLine = null;

    // Debug stuff
    Vector2f checkpointDirection = new Vector2f(0, 0);
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
        Vector2f carDirection = getCarDirection();
        Line carLine = new Line(info.getX(), info.getY(), info.getX() + carDirection.getX(), info.getY() + carDirection.getY());

        // Get closest obstacle
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Polygon polygon : polygons) {
            for (int i = 0; i < polygon.npoints; i++) {
                Line obstacleLine = new Line(polygon.xpoints[i], polygon.ypoints[i], polygon.xpoints[(i + 1) % polygon.npoints], polygon.ypoints[(i + 1) % polygon.npoints]);
                double distance = intersects(carLine, obstacleLine);
                if (distance > 0 && distance <= collisionAvoidanceRadius && distance < closestDistance) {
                    closestObstacleLine = obstacleLine;
                    closestDistance = distance;
                }
            }
        }

        // No obstacle in reach
        if (closestObstacleLine == null)
            return null;

        // Calculate the orthogonal vector to use for collision avoidance for found obstacle
        Vector2f obsDirection = closestObstacleLine.getDirection();

        Vector2f orth1;
        Vector2f orth2;
        if (Math.abs(closestObstacleLine.getM()) == Double.MAX_VALUE) {
            orth1 = new Vector2f(-1, 0);
            orth2 = new Vector2f(1, 0);
        } else {
            orth1 = new Vector2f(-obsDirection.getY(), obsDirection.getX());
            orth2 = new Vector2f(obsDirection.getY(), -obsDirection.getX());
        }
        Vector2f center = closestObstacleLine.getCenter();

        if (Point.distance(info.getX(), info.getY(), orth1.getX() + center.getX(), orth1.getY() + center.getY()) <
                Point.distance(info.getX(), info.getY(), orth2.getX() + center.getX(), orth2.getY() + center.getY()))
            return new Pair<>(orth1, closestDistance);
        else
            return new Pair<>(orth2, closestDistance);
    }

    /**
     * Checks if two lines intersect each other.
     * @param first The first line
     * @param second The second line
     * @return The distance to the intersection
     */
    private double intersects(Line first, Line second) {
        // Parallel or on-top of each other
        if (first.getM() == second.getM())
            return -1;

        double x = (first.getB() - second.getB()) / (second.getM() - first.getM());
        double y = first.getM() * x + first.getB();

        // Horizontal line -> Check only x
        if (second.getM() == 0) {
            if (valueBetween(x, second.getX1(), second.getX2())) {
                return Point.distance(x, y, info.getX(), info.getY());
            }
        }
        // Vertical line -> Check only y
        else if (Math.abs(second.getM()) == Double.MAX_VALUE) {
            x = second.getX1();
            y = first.getM() * x + first.getB();
            if (valueBetween(y, second.getY1(), second.getY2())) {
                return Point.distance(x, y, info.getX(), info.getY());
            }
        }
        // Check both
        else if (valueBetween(x, second.getX1(), second.getX2()) || valueBetween(y, second.getY1(), second.getY2())) {
            return Point.distance(x, y, info.getX(), info.getY());
        }
        return -1;
    }

    private boolean valueBetween(double value, double a, double b) {
        return (value >= a && value <= b) || (value <= a && value >= b);
    }

    private void doDebugStuff() {
        ai.addDebugAction(() -> {
            Vector2f carPosition = new Vector2f(info.getX(), info.getY());
            GLUtil.drawLine(carPosition, checkpointDirection, Color.GREEN);
            GLUtil.drawLine(carPosition, avoidanceDirection, Color.RED);

            // Current obstacle
            if (closestObstacleLine != null)
                GLUtil.drawLine(closestObstacleLine.getX1(), closestObstacleLine.getY1(), closestObstacleLine.getX2(), closestObstacleLine.getY2(), Color.WHITE);
        });
    }
}
