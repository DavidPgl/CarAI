package s0564478.graph;

import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class OffsetPolygon extends Polygon {
    private final Vector2f[] normalVectors;

    public OffsetPolygon(int[] xpoints, int[] ypoints, int npoints, Vector2f[] normalVectors) {
        super(xpoints, ypoints, npoints);
        this.normalVectors = normalVectors;
    }

    public Vector2f[] getNormalVectors() {
        return normalVectors;
    }
}
