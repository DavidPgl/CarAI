package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import s0564478.CarAI;

public class ThrottleBehaviour {
    private static final float goalRadius = 1.8f;
    private static final float decelerateRadius = 15.8f;
    private static final float throttleTime = 1.2f;

    private final Info info;
    private final CarAI ai;

    public ThrottleBehaviour(Info info, CarAI ai) {
        this.info = info;
        this.ai = ai;
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
}
