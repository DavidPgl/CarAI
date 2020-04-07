package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class CarAI extends AI{
    private Point checkpoint;

    private static final float goalRadius = 1;
    private static final float decelerateRadius = 20;
    private static final float accTime = 1;

    // In Degree
    private static final float goalAngle = 1;
    private static final float decelerateAngle = 70;


    public CarAI(Info info) {
        super(info);
        enlistForInternalDevelopmentPurposesOnlyAndDoNOTConsiderThisAsPartOfTheHandedInSolution();
    }

    @Override
    public String getName() {
        return "Kaes3din0";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        checkpoint = info.getCurrentCheckpoint();


        return new DriverAction(getThrottle(), getSteering());
    }

    @Override
    public String getTextureResourceName() {
        return "/s0564478/art/car.png";
    }

    private float getThrottle() {
        double distance = checkpoint.distance(info.getX(), info.getY());

        if(distance < goalRadius)
            return 0;
        else if(distance < decelerateRadius) {
            double newSpeed = (distance / decelerateRadius) * info.getMaxAbsoluteAcceleration();
            double speedDiff = newSpeed - info.getVelocity().length();
            return (float) (speedDiff / accTime);
        }
        else
            return info.getMaxAbsoluteAcceleration();
    }

    private float getSteering() {
        Vector2f direction = new Vector2f((float) checkpoint.getX() - info.getX(), (float) checkpoint.getY() - info.getY());
        double orientation = info.getOrientation();
        double diffAngle = getAngle(new Vector2f((float) Math.cos(orientation), (float) Math.sin(orientation)), direction);
        if(diffAngle < goalAngle)
            return 0;
        else if(diffAngle < decelerateAngle) {
            double newAngularSpeed = (diffAngle / decelerateAngle) * info.getMaxAbsoluteAngularAcceleration();
            return (float) ((newAngularSpeed - info.getAngularVelocity()) / 0.001);
        }
        else
            return info.getMaxAbsoluteAngularAcceleration();
    }

    private double dot(Vector2f first, Vector2f second){
        return first.getX() * second.getX() + first.getY() * second.getY();
    }

    /**
     * Calculates the angle between the given vectors
     * @return Angle in degree
     */
    private double getAngle(Vector2f from, Vector2f to){
        return Math.toDegrees(Math.acos(dot(from, to) / (from.length() * to.length())));
    }

    @Override
    public void doDebugStuff() {
        double orientation = info.getOrientation();
        Vector2f direction = new Vector2f((float) Math.cos(orientation), (float) Math.sin(orientation));
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(1,0,0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getX() + direction.getX() * 10, info.getY() + direction.getY() * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(0,1,0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY());
        GL11.glEnd();
    }
}
