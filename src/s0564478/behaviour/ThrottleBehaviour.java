package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import s0564478.CarAI;

public class ThrottleBehaviour {
    private final Info info;
    private final CarAI ai;

    private static final float goalRadius = 6.58f;
    private static final float decelerateRadius = 14.22f;
    private static final float throttleTime = 4.11f;

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

        System.out.println(throttle);
        return throttle;
    }
}
