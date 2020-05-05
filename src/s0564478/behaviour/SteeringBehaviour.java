package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;
import s0564478.CarAI;
import s0564478.util.GLUtil;
import s0564478.util.Line;
import s0564478.util.VectorUtil;

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

        doDebugStuff();

        return steering;
    }

    private float getSteeringTo(Vector2f destinationDirection) {
        Vector2f carDirection = getCarDirection();
        // Get angle between current and destination direction
        double diffAngle = Vector2f.angle(carDirection, destinationDirection);
        diffAngle = Math.toDegrees(diffAngle);
        boolean angleIsNegative = VectorUtil.angleIsClockwise(carDirection, destinationDirection);

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

    private Vector2f getCarDirection() {
        float orientation = info.getOrientation();
        return new Vector2f((float) Math.cos(orientation), (float) Math.sin(orientation));
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
