package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import s0564478.CarAI;

public class ThrottleBehaviour {
    private final Info info;
    //private final CarAI ai;

    private float goalRadius = 4f;
    private float decelerateRadius = 5.8f;
    private float throttleTime = 3f;


    public ThrottleBehaviour(Info info, CarAI ai) {
        this.info = info;
        //this.ai = ai;
    }

    public ThrottleBehaviour(Info info, float goalRadius, float decelerateRadius, float throttleTime) {
        this.info = info;
        this.goalRadius = goalRadius;
        this.decelerateRadius = decelerateRadius;
        this.throttleTime = throttleTime;
    }

    public float getThrottle() {
        double distance = info.getCurrentCheckpoint().distance(info.getX(), info.getY());

        if (distance < goalRadius)
            return 0;
        else if (distance < decelerateRadius) {
            double newSpeed = (distance / decelerateRadius) * info.getMaxAbsoluteAcceleration();
            double speedDiff = newSpeed - info.getVelocity().length();
            return (float) (speedDiff / throttleTime);
        } else
            return info.getMaxAbsoluteAcceleration();
    }

    public float getGoalRadius() {
        return goalRadius;
    }

    public float getDecelerateRadius() {
        return decelerateRadius;
    }

    public float getThrottleTime() {
        return throttleTime;
    }
}
