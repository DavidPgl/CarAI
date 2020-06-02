package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;
import s0564478.CarAI;
import s0564478.util.VectorUtil;

import java.awt.*;

public class SteeringBehaviour {

    private final Info info;
    private final CarAI ai;


    public SteeringBehaviour(Info info, CarAI ai) {
        this.info = info;
        this.ai = ai;

    }

    public float getSteering(Point nextRoutePoint) {
        Vector2f nextPointDirection = new Vector2f((float) nextRoutePoint.getX() - info.getX(), (float) nextRoutePoint.getY() - info.getY());
        return getSteeringTo(nextPointDirection);
    }

    private float getSteeringTo(Vector2f destinationDirection) {
        Vector2f carDirection = ai.getCarDirection();
        // Get angle between current and destination direction
        double diffAngle = Vector2f.angle(carDirection, destinationDirection);
        diffAngle = Math.toDegrees(diffAngle);
        boolean angleIsNegative = VectorUtil.angleIsClockwise(carDirection, destinationDirection);

        // Calculate steering velocity
        if (diffAngle < BehaviourStats.GOAL_ANGLE)
            return 0;
        else if (diffAngle < BehaviourStats.DECELERATE_ANGLE) {
            double newVelocity = diffAngle / BehaviourStats.DECELERATE_ANGLE * info.getMaxAbsoluteAngularVelocity() * (angleIsNegative ? -1 : 1);
            float newAcceleration = (float) ((newVelocity - info.getAngularVelocity()) / BehaviourStats.STEERING_TIME);
            return Math.max(Math.min(newAcceleration, info.getMaxAbsoluteAngularAcceleration()), -info.getMaxAbsoluteAngularAcceleration());
        } else
            return info.getMaxAbsoluteAngularAcceleration() * (angleIsNegative ? -1 : 1);
    }

    private void doDebugStuff() {
    }
}
