package s0564478.graph;

import org.lwjgl.util.vector.Vector2f;
import s0564478.util.VectorUtil;

import java.awt.*;

public class OffsetPolygon extends Polygon {
    public final Polygon basePolygon;
    private final Vector2f[] normalVectors;

    public static OffsetPolygon generateOffSetPolygon(Polygon basePolygon, float offset) {
        int[] xPoints = new int[basePolygon.npoints];
        int[] yPoints = new int[basePolygon.npoints];
        Vector2f[] normalVectors = new Vector2f[basePolygon.npoints];
        for (int i = 0; i < basePolygon.npoints; i++) {
            Point previous = new Point(basePolygon.xpoints[i], basePolygon.ypoints[i]);
            Point current = new Point(basePolygon.xpoints[(i + 1) % basePolygon.npoints],
                    basePolygon.ypoints[(i + 1) % basePolygon.npoints]);
            Point next = new Point(basePolygon.xpoints[(i + 2) % basePolygon.npoints],
                    basePolygon.ypoints[(i + 2) % basePolygon.npoints]);

            Vector2f a = VectorUtil.vectorFromPoints(current, previous).normalise(null);
            Vector2f b = VectorUtil.vectorFromPoints(current, next).normalise(null);

            // Move current outwards using both orth-vectors
            Vector2f orthA = new Vector2f(-a.getY(), a.getX());
            Vector2f orthB = new Vector2f(b.getY(), -b.getX());
            Vector2f normalVector = VectorUtil.add(orthA, orthB).normalise(null);
            Vector2f translateVector = VectorUtil.scale(normalVector, offset);

            current.translate((int) translateVector.getX(), (int) translateVector.getY());
            // Save values
            xPoints[i] = (int) current.getX();
            yPoints[i] = (int) current.getY();
            normalVectors[i] = normalVector;
        }
        return new OffsetPolygon(xPoints, yPoints, basePolygon.npoints, basePolygon, normalVectors);
    }

    private OffsetPolygon(int[] xpoints, int[] ypoints, int npoints, Polygon basePolygon, Vector2f[] normalVectors) {
        super(xpoints, ypoints, npoints);
        this.normalVectors = normalVectors;
        this.basePolygon = basePolygon;
    }

    public Vector2f[] getNormalVectors() {
        return normalVectors;
    }
}
