package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class LastWinner5 extends AI {
    private Point checkpoint;

    private float goalRadius = 1.8f;
    private float decelerateRadius = 15.8f;
    private float throttleTime = 1.2f;

    // In Degree
    private float goalAngle = 1.2f;
    private float decelerateAngle = 47.6f;
    private float steerTime = 0.3f;


    public LastWinner5(Info info) {
        super(info);
        enlistForTournament(564478, 562886);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        checkpoint = info.getCurrentCheckpoint();

        float steering = getSteering();
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

    private float getSteering() {
        Vector2f checkpointDirection = new Vector2f((float) checkpoint.getX() - info.getX(), (float) checkpoint.getY() - info.getY());
        double carDirectionRadiant = info.getOrientation();
        Vector2f carDirection = new Vector2f((float) Math.cos(carDirectionRadiant), (float) Math.sin(carDirectionRadiant));
        double diffAngle = Vector2f.angle(carDirection, checkpointDirection);
        diffAngle = Math.toDegrees(diffAngle);

        boolean angleIsNegative = angleIsNegative(carDirection, checkpointDirection);

        if (diffAngle < goalAngle)
            return 0;
        else if (diffAngle < decelerateAngle) {
            double newVelocity = diffAngle / decelerateAngle * info.getMaxAbsoluteAngularVelocity() * (angleIsNegative ? -1 : 1) ;
            float newAcceleration = (float) ((newVelocity - info.getAngularVelocity()) / steerTime);
            return Math.max(Math.min(newAcceleration, info.getMaxAbsoluteAngularAcceleration()), -info.getMaxAbsoluteAngularAcceleration());
        } else
            return info.getMaxAbsoluteAngularAcceleration() * (angleIsNegative ? -1 : 1);
    }

    private boolean angleIsNegative(Vector2f from, Vector2f to) {
        return 0 >= from.getX() * to.getY() - from.getY() * to.getX();
    }

    /*

    @Override
    public void doDebugStuff() {
        double orientation = info.getOrientation();
        Vector2f direction = new Vector2f((float) Math.cos(orientation), (float) Math.sin(orientation));
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(1, 0, 0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getX() + direction.getX() * 10, info.getY() + direction.getY() * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(0, 1, 0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY());
        GL11.glEnd();
    }

     */
}
