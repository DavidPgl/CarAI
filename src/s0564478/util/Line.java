package s0564478.util;

import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class Line {
    private final double m;
    private final double b;
    private double x1, x2, y1, y2;

    public Line(double m, double b) {
        this.m = m;
        this.b = b;
    }

    public Line(double x1, double y1, double x2, double y2) {
        this.m = Math.max(-Double.MAX_VALUE, Math.min((y2 - y1) / (x2 - x1), Double.MAX_VALUE));
        this.b = Math.max(-Double.MAX_VALUE, Math.min(y1 - m * x1, Double.MAX_VALUE));
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public double getM() {
        return m;
    }

    public double getB() {
        return b;
    }

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public double getY1() {
        return y1;
    }

    public double getY2() {
        return y2;
    }

    public Vector2f getDirection() {
        return new Vector2f(1, (float) m).normalise(null);
    }

    public Vector2f getCenter() {
        return new Vector2f((float) (x1 + x2) / 2f, (float) (y1 + y2) / 2f);
    }

    /**
     * Checks if two lines intersect each other.
     *
     * @param first  The first line
     * @param second The second line
     * @return The intersection point.
     */
    private Point intersects(Line first, Line second) {
        // Parallel or on-top of each other
        if (first.getM() == second.getM())
            return null;

        double x = (first.getB() - second.getB()) / (second.getM() - first.getM());
        double y = first.getM() * x + first.getB();

        Point point = new Point((int) x, (int) y);

        // Horizontal line -> Check only x
        if (second.getM() == 0) {
            if (valueBetween(x, second.getX1(), second.getX2())) {
                return point;
            }
        }
        // Vertical line -> Check only y
        else if (Math.abs(second.getM()) == Double.MAX_VALUE) {
            x = second.getX1();
            y = first.getM() * x + first.getB();
            if (valueBetween(y, second.getY1(), second.getY2())) {
                return point;
            }
        }
        // Check both
        else if (valueBetween(x, second.getX1(), second.getX2()) || valueBetween(y, second.getY1(), second.getY2())) {
            return point;
        }
        return null;
    }

    private boolean valueBetween(double value, double a, double b) {
        return (value >= a && value <= b) || (value <= a && value >= b);
    }
}
