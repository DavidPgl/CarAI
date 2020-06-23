package s0564478.navigation;

import java.awt.*;

public class LevelPoint extends Point {
    public final Type type;
    public LevelZone levelZone;

    /**
     * Angle between 0 and pi (radians)
     */
    private float angleToNextPoint;

    public LevelPoint(Point p, Type type) {
        this(p, type, null);
    }

    public LevelPoint(int x, int y, Type type) {
        this(new Point(x, y), type, null);
    }

    public LevelPoint(Point p, Type type, LevelZone levelZone) {
        super(p);
        this.type = type;
        this.levelZone = levelZone;
    }

    public void setAngleToNextPoint(float angleToNextPoint) {
        this.angleToNextPoint = angleToNextPoint;
    }

    public float getAngleToNextPoint() {
        return angleToNextPoint;
    }

    public enum Type {STEP_POINT, ROUTE_POINT, CHECKPOINT}
}
