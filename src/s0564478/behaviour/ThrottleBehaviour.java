package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import s0564478.CarAI;

public class ThrottleBehaviour {
    private final Info info;
    private final CarAI ai;

    private static final float goalRadius = 1.2f;
    private static final float decelerateRadius = 15.8f;
    private static final float throttleTime = 1.2f;

    private static final float slowSpeedPercentage = 0.1f;


    public ThrottleBehaviour(Info info, CarAI ai) {
        this.info = info;
        this.ai = ai;
    }

    public float getThrottle(boolean driveSlower) {
        double distance = info.getCurrentCheckpoint().distance(info.getX(), info.getY());
        float throttle;

        if (distance < goalRadius)
            throttle = 0;
        else if (distance < decelerateRadius) {
            double newSpeed = (distance / decelerateRadius) * info.getMaxAbsoluteAcceleration();
            double speedDiff = newSpeed - info.getVelocity().length();
            throttle = (float) (speedDiff / throttleTime);
        } else
            throttle = info.getMaxAbsoluteAcceleration();

        float slowSpeed = info.getMaxAbsoluteAcceleration() * slowSpeedPercentage;
        if (driveSlower && throttle > slowSpeed)
            throttle = slowSpeed;

        return throttle;
    }
}
