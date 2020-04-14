package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.Random;

public class CarAI extends AI {
    private Point checkpoint;

    private float goalRadius = 1;
    private float decelerateRadius = 30;
    private float throttleTime = 2f;

    // In Degree
    private float goalAngle = 1;
    private float decelerateAngle = 90;
    private float steerTime = 1f;

    private Vector2f obstacleDirection = new Vector2f(0,0);
    private Vector2f orthDirection = new Vector2f(0,0);
    private Line closestLine = null;
    private Line carLine = new Line(0,0,0,0);

    public CarAI(Info info) {
        super(info);
//        Random random = new Random();
//        goalRadius = random.nextFloat() * 5;
//        decelerateRadius = goalRadius + random.nextFloat() * 100;
//        throttleTime = 0.1f + random.nextFloat() * 5;
//
//        goalAngle = random.nextFloat() * 10;
//        decelerateAngle = goalRadius + random.nextFloat() * 135;
//        steerTime = 0.1f + random.nextFloat() * 5;

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
        float steering = getSteering(checkpointDirection);
        Vector2f orthogonalDirection = getCollisionAvoidance(20);
        orthDirection = orthogonalDirection == null ? new Vector2f(0,0) : orthogonalDirection;

        float obstacleSteering = getSteering(orthogonalDirection);

        return new DriverAction(getThrottle(), obstacleSteering > 0 ? obstacleSteering : steering);
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
        if (destinationDirection == null)
            return 0;

        Vector2f carDirection = getCarDirection();
        System.out.println(carDirection);

        double diffAngle = Vector2f.angle(carDirection, destinationDirection);
        diffAngle = Math.toDegrees(diffAngle);

        boolean angleIsNegative = angleIsNegative(carDirection, destinationDirection);

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


    private Vector2f getCollisionAvoidance(double minimumDistance) {
        Polygon[] polygons = info.getTrack().getObstacles();
        Vector2f carDirection = getCarDirection();
        carLine = new Line(info.getX(), info.getY(), info.getX() + carDirection.getX(), info.getY() + carDirection.getY());

        // Get closest obstacle
        closestLine = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Polygon polygon : polygons) {
            for (int i = 0; i < polygon.npoints; i++) {

                Line obstacleLine = new Line(polygon.xpoints[i], polygon.ypoints[i], polygon.xpoints[(i + 1) % polygon.npoints], polygon.ypoints[(i + 1) % polygon.npoints]);


                // Parallel or on-top of each other
                if (carLine.m == obstacleLine.m)
                    continue;

                double x = (carLine.b - obstacleLine.b) / (obstacleLine.m - carLine.m);
                double y = carLine.m * x + carLine.b;

                if(((x >= obstacleLine.x1 && x <= obstacleLine.x2) || (x <= obstacleLine.x1 && x >= obstacleLine.x2)) &&
                        ((y >= obstacleLine.y1 && y <= obstacleLine.y2) || (y <= obstacleLine.y1 && y >= obstacleLine.y2))) {

                    double distance = Point.distance(x, y, info.getX(), info.getY());

                    if (distance <= minimumDistance && distance < closestDistance) {
                        closestLine = obstacleLine;
                        closestDistance = distance;
                    }
                }

            }
        }
        // No obstacle in reach
        if (closestLine == null) {
            obstacleDirection = new Vector2f(0,0);
            return null;
        }


        Vector2f obsDirection = closestLine.getDirection();
        obstacleDirection = obsDirection;

        Vector2f orth1 = new Vector2f(-obsDirection.getY(), obsDirection.getX());
        Vector2f orth2 = new Vector2f(obsDirection.getY(), -obsDirection.getX());

        if (Point.distance(info.getX(), info.getY(), orth1.getX(), orth1.getY()) >
                Point.distance(info.getX(), info.getY(), orth2.getX(), orth2.getY()))
            return orth1;
        else
            return orth2;

    }

    private static class Line {
        private final double m;
        private final double b;
        private double x1,x2,y1,y2;

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
    }

    @Override
    public void doDebugStuff() {

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(1, 0, 0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getX() + obstacleDirection.getX() * 10, info.getY() + obstacleDirection.getY() * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(0, 1, 0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getX() + orthDirection.getX() * 10, info.getY() + orthDirection.getY() * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(0, 0, 1);
        GL11.glVertex2d(carLine.x1, carLine.y1);
        GL11.glVertex2d(carLine.x2 + carLine.getDirection().getX() * 10, carLine.y2 + carLine.getDirection().getY() * 10);
        GL11.glEnd();

        if(closestLine != null) {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor3f(1, 1, 1);
            GL11.glVertex2d(closestLine.x1, closestLine.y1);
            GL11.glVertex2d(closestLine.x2, closestLine.y2);
            GL11.glEnd();
        }
    }
}
