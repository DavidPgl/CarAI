package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import s0564478.util.Pair;
import s0564478.util.VectorUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class CarAI extends AI {
    private Point checkpoint;

    private static final float goalRadius = 1.8f;
    private static final float decelerateRadius = 15.8f;
    private static final float throttleTime = 1.2f;

    // In Degree
    private static final float goalAngle = 1.2f;
    private static final float decelerateAngle = 47.6f;
    private static final float steerTime = 0.3f;

    private static final float collisionAvoidanceRadius = 50.0f;

    private Line closestObstacleLine = null;

    // Debug stuff
    private Vector2f checkpointDirection = new Vector2f(0, 0);
    private Vector2f avoidanceDirection = new Vector2f(0, 0);

    public CarAI(Info info) {
        super(info);

        try {
            File values = new File(System.getProperty("user.dir") + "/values.txt");
            String data = this.getClass().getSimpleName() + ":  " + String.format("%.1f | %.1f | %.1f | %.1f | %.1f | %.1f", goalRadius, decelerateRadius, throttleTime, goalAngle, decelerateAngle, steerTime) + "\n";
            Files.write(values.toPath(), data.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        enlistForTournament(564478, 562886);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        checkpoint = info.getCurrentCheckpoint();

        Vector2f checkpointDirection = new Vector2f((float) checkpoint.getX() - info.getX(), (float) checkpoint.getY() - info.getY());
        Pair<Vector2f, Double> collisionAvoidance = getCollisionAvoidance();

        // --- DEBUG ONLY ---
        this.checkpointDirection = checkpointDirection.normalise(null);
        avoidanceDirection = collisionAvoidance == null ? new Vector2f(0, 0) : collisionAvoidance.getFirst().normalise(null);
        // ------------------

        float steering = getSteering(checkpointDirection);

        if (collisionAvoidance != null) {
            float avoidanceSteering = getSteering(collisionAvoidance.getFirst());
            double weight = collisionAvoidance.getSecond() / collisionAvoidanceRadius;
            steering = (float) (steering * weight + avoidanceSteering * (1 - weight));
        }

        return new DriverAction(getThrottle(), steering);
    }

    @Override
    public String getTextureResourceName() {
        return "/s0564478/art/car.png";
    }

    private float getThrottle() {
        double distance = checkpoint.distance(info.getX(), info.getY());

        if (distance < goalRadius)
            return 0;
        else if (distance < decelerateRadius) {
            double newSpeed = (distance / decelerateRadius) * info.getMaxAbsoluteAcceleration();
            double speedDiff = newSpeed - info.getVelocity().length();
            return (float) (speedDiff / throttleTime);
        } else
            return info.getMaxAbsoluteAcceleration();
    }

    private float getSteering(Vector2f destinationDirection) {
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
        double carDirectionRadiant = info.getOrientation();
        return new Vector2f((float) Math.cos(carDirectionRadiant), (float) Math.sin(carDirectionRadiant));
    }


    private Pair<Vector2f, Double> getCollisionAvoidance() {
        Polygon[] polygons = info.getTrack().getObstacles();
        Vector2f carDirection = getCarDirection();
        Line carLine = new Line(info.getX(), info.getY(), info.getX() + carDirection.getX(), info.getY() + carDirection.getY());

        // Get closest obstacle
        closestObstacleLine = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Polygon polygon : polygons) {
            for (int i = 0; i < polygon.npoints; i++) {

                Line obstacleLine = new Line(polygon.xpoints[i], polygon.ypoints[i], polygon.xpoints[(i + 1) % polygon.npoints], polygon.ypoints[(i + 1) % polygon.npoints]);


                // Parallel or on-top of each other
                if (carLine.m == obstacleLine.m)
                    continue;

                double x = (carLine.b - obstacleLine.b) / (obstacleLine.m - carLine.m);
                double y = carLine.m * x + carLine.b;

                // Horizontal line -> Check only x
                if (obstacleLine.m == 0) {
                    if (valueBetween(x, obstacleLine.x1, obstacleLine.x2)) {
                        double distance = Point.distance(x, y, info.getX(), info.getY());
                        if (distance <= collisionAvoidanceRadius && distance < closestDistance) {
                            closestObstacleLine = obstacleLine;
                            closestDistance = distance;

                        }
                    }
                }
                // Vertical line -> Check only y
                else if (Math.abs(obstacleLine.m) == Double.MAX_VALUE) {
                    x = obstacleLine.x1;
                    y = carLine.m * x + carLine.b;
                    if (valueBetween(y, obstacleLine.y1, obstacleLine.y2)) {
                        double distance = Point.distance(x, y, info.getX(), info.getY());
                        if (distance <= collisionAvoidanceRadius && distance < closestDistance) {
                            closestObstacleLine = obstacleLine;
                            closestDistance = distance;
                        }
                    }
                }
                // Check both
                else if (valueBetween(x, obstacleLine.x1, obstacleLine.x2) || valueBetween(y, obstacleLine.y1, obstacleLine.y2)) {
                    double distance = Point.distance(x, y, info.getX(), info.getY());
                    if (distance <= collisionAvoidanceRadius && distance < closestDistance) {
                        closestObstacleLine = obstacleLine;
                        closestDistance = distance;
                    }
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
        if (Math.abs(closestObstacleLine.m) == Double.MAX_VALUE) {
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

    private boolean valueBetween(double value, double a, double b) {
        return (value >= a && value <= b) || (value <= a && value >= b);
    }

    private static class Line {
        private final double m;
        private final double b;
        private double x1, x2, y1, y2;

        public Line(double m, double b) {
            this.m = m;
            this.b = b;
        }

        public Line(double x1, double y1, double x2, double y2) {
            this.m = Math.max(-Double.MAX_VALUE, Math.min((y2 - y1) / (x2 - x1), Double.MAX_VALUE));
            this.b = Math.max(-Double.MAX_VALUE, Math.min(y1 - m * x1, Double.MAX_VALUE));
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        public Vector2f getDirection() {
            return new Vector2f(1, (float) m).normalise(null);
        }

        public Vector2f getCenter() {
            return new Vector2f((float) (x1 + x2) / 2f, (float) (y1 + y2) / 2f);
        }
    }

    @Override
    public void doDebugStuff() {
        Vector2f carPosition = new Vector2f(info.getX(), info.getY());
        drawLine(carPosition, checkpointDirection, Color.GREEN);
        drawLine(carPosition, avoidanceDirection, Color.RED);

        // Current obstacle
        if (closestObstacleLine != null)
            drawLine(closestObstacleLine.x1, closestObstacleLine.y1, closestObstacleLine.x2, closestObstacleLine.y2, Color.WHITE);
    }

    private void drawLine(Vector2f point, Vector2f direction, Color color) {
        drawLine(point.x, point.y, point.x + direction.x * 10, point.y + direction.y * 10, color);
    }

    private void drawLine(double x1, double y1, double x2, double y2, Color color) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(color.getRed(), color.getGreen(), color.getBlue());
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }
}
