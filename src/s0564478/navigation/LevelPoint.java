package s0564478.navigation;

import java.awt.*;

public class LevelPoint extends Point {
    public final Type type;


    /**
     * Angle between 0 and pi (radians)
     */
    private float angleToNextPoint;

    public LevelPoint(Point p, Type type) {
        super(p);
        this.type = type;
    }

    public LevelPoint(int x, int y, Type type) {
        super(x, y);
        this.type = type;
    }

    public void setAngleToNextPoint(float angleToNextPoint) {
        this.angleToNextPoint = angleToNextPoint;
    }

    public float getAngleToNextPoint() {
        return angleToNextPoint;
    }

    public enum Type {STEP_POINT, ROUTE_POINT, CHECKPOINT}
}
