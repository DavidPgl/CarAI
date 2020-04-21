package s0564478.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class GLUtil {
    public static void drawLine(Vector2f point, Vector2f direction, Color color) {
        direction.normalise(direction);
        drawLine(point.x, point.y, point.x + direction.x * 10, point.y + direction.y * 10, color);
    }

    public static void drawLine(double x1, double y1, double x2, double y2, Color color) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(color.getRed(), color.getGreen(), color.getBlue());
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }
}
