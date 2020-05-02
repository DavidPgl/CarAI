package s0564478.testUtil;

import java.util.Random;

public class RandomExtender extends Random {

    public float between(float min, float max) {
        return nextFloat() * (max - min) + min;
    }
}
