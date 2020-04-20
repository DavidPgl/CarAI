package s0564478.util;

import org.lwjgl.util.vector.Vector2f;

public class VectorUtil {
    public static Vector2f add(Vector2f first, Vector2f second) {
        return new Vector2f(first.x + second.x, first.y + second.y);
    }

    public static Vector2f multiply(Vector2f vector, float factor) {
        return new Vector2f(vector.x * factor, vector.y * factor);
    }
}
