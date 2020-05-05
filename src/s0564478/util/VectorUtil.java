package s0564478.util;

import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class VectorUtil {
    public static Vector2f add(Vector2f first, Vector2f second) {
        return new Vector2f(first.x + second.x, first.y + second.y);
    }

    public static Vector2f multiply(Vector2f vector, float factor) {
        return new Vector2f(vector.x * factor, vector.y * factor);
    }

    /**
     * Checks if the angles direction is clockwise (aka negative)
     *
     * @param from Vector a
     * @param to   Vector b
     * @return Whether the angles direction is clockwise (negative)
     */
    public static boolean angleIsClockwise(Vector2f from, Vector2f to) {
        return 0 >= from.getX() * to.getY() - from.getY() * to.getX();
    }

    public static Vector2f vectorFromPoints(Point from, Point to) {
        return new Vector2f((float) (to.getX() - from.getX()), (float) (to.getY() - from.getY()));
    }
}
