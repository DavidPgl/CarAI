package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import s0564478.CarAI;

public class ThrottleBehaviour {
    private final Info info;
    private final CarAI ai;

    private static final float goalRadius = 5.38f;
    private static final float decelerateRadius = 9.88f;
    private static final float throttleTime = 1.08f;


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
