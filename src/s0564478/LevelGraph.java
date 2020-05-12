package s0564478;

import org.lwjgl.util.vector.Vector2f;
import s0564478.graph.Graph;
import s0564478.graph.OffsetPolygon;
import s0564478.graph.Vertex;
import s0564478.util.VectorUtil;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * How we planned everything (BTS):
 * <a href="https://gyazo.com/e69bd83f009b11c91f1a40795e3203e2">Best sketch ever:P</a>
 */
public class LevelGraph {
    private static final int POINT_OFFSET = 20;
    private final Graph<Point> graph = new Graph<>();
    private final OffsetPolygon[] offsetObstacles;

    public LevelGraph(Polygon[] obstacles) {
        this.offsetObstacles = generateOffsetPolygons(obstacles);

        addObstacleVerticesToGraph(offsetObstacles);
        addFreeEdges();
    }

    public void updateCarAndCP(Point carPosition, Point checkpointPosition) {
        addFreeEdgesForVertex(graph.add(carPosition));
        addFreeEdgesForVertex(graph.add(checkpointPosition));
    }

    public List<Vertex<Point>> getVertices() {
        return graph.getVertices();
    }

    public List<Point> getVerticesPoints() {
        return graph.getVertices().stream().map(Vertex::getData).collect(Collectors.toList());
    }

    /**
     * Goes through the polygon list and applies an offset ({@link #POINT_OFFSET}) to it
     *
     * @param polygons All polygons that will be translated.
     */
    private OffsetPolygon[] generateOffsetPolygons(Polygon[] polygons) {
        OffsetPolygon[] offsetPolygons = new OffsetPolygon[polygons.length];
        for (int n = 0; n < polygons.length; n++) {
            Polygon polygon = polygons[n];
            int[] xPoints = new int[polygon.npoints];
            int[] yPoints = new int[polygon.npoints];
            Vector2f[] normalVectors = new Vector2f[polygon.npoints];
            for (int i = 0; i < polygon.npoints; i++) {
                Point previous = new Point(polygon.xpoints[i], polygon.ypoints[i]);
                Point current = new Point(polygon.xpoints[(i + 1) % polygon.npoints], polygon.ypoints[(i + 1) % polygon.npoints]);
                Point next = new Point(polygon.xpoints[(i + 2) % polygon.npoints], polygon.ypoints[(i + 2) % polygon.npoints]);

                Vector2f a = VectorUtil.vectorFromPoints(current, previous);
                Vector2f b = VectorUtil.vectorFromPoints(current, next);


                // Move current outwards using both orth-vectors
                Vector2f orthA = new Vector2f(-a.getY(), a.getX());
                Vector2f orthB = new Vector2f(b.getY(), -b.getX());
                Vector2f normalVector = VectorUtil.add(orthA, orthB).normalise(null);
                Vector2f translateVector = VectorUtil.scale(normalVector, POINT_OFFSET);
                current.translate((int) translateVector.getX(), (int) translateVector.getY());
                // Save values
                xPoints[i] = (int) current.getX();
                yPoints[i] = (int) current.getY();
                normalVectors[i] = normalVector;
            }
            offsetPolygons[n] = new OffsetPolygon(xPoints, yPoints, polygon.npoints, normalVectors);
        }
        return offsetPolygons;
    }


    /**
     * Adds all convex corners as vertices to the graph.
     *
     * @param obstacles All obstacles in the level.
     */
    private void addObstacleVerticesToGraph(Polygon[] obstacles) {
        for (Polygon obstacle : obstacles) {
            for (int i = 0; i < obstacle.npoints; i++) {
                Point previous = new Point(obstacle.xpoints[i], obstacle.ypoints[i]);
                Point current = new Point(obstacle.xpoints[(i + 1) % obstacle.npoints], obstacle.ypoints[(i + 1) % obstacle.npoints]);
                Point next = new Point(obstacle.xpoints[(i + 2) % obstacle.npoints], obstacle.ypoints[(i + 2) % obstacle.npoints]);

                Vector2f a = VectorUtil.vectorFromPoints(current, previous);
                Vector2f b = VectorUtil.vectorFromPoints(current, next);

                // If angle is clockwise (negative) the corner points outwards (convex corner, or anti-concave).
                if (VectorUtil.angleIsClockwise(a, b))
                    graph.add(current);
            }
        }
    }

    private void addFreeEdges() {
        for (Vertex<Point> currentVertex : graph.getVertices()) {
            addFreeEdgesForVertex(currentVertex);
        }
    }

    private void addFreeEdgesForVertex(Vertex<Point> currentVertex) {
        for (Vertex<Point> nextVertex : graph.getVertices()) {
            // Skip same Vertex
            if (nextVertex.equals(currentVertex))
                continue;
            // If any obstacle is intersected, skip
            if (checkForIntersection(currentVertex.getData(), nextVertex.getData()))
                continue;
            int weight = (int) currentVertex.getData().distance(nextVertex.getData());
            graph.addEdge(currentVertex.getData(), nextVertex.getData(), weight);
        }
    }

    private boolean checkForIntersection(Point first, Point second) {
        for (OffsetPolygon obstacle : offsetObstacles) {
            for (int i = 0; i < obstacle.npoints; i++) {
                Point a = new Point(obstacle.xpoints[i], obstacle.ypoints[i]);
                int nextI = (i + 1) % obstacle.npoints;
                Point b = new Point(obstacle.xpoints[nextI], obstacle.ypoints[nextI]);

                // Move points a bit to prevent unintended intersections with obstacles (at their position)
                a = VectorUtil.translatePoint(a, VectorUtil.scale(obstacle.getNormalVectors()[i], -3));
                b = VectorUtil.translatePoint(b, VectorUtil.scale(obstacle.getNormalVectors()[nextI], -3));

                if (Line2D.linesIntersect(a.getX(), a.getY(), b.getX(), b.getY(),
                        first.getX(), first.getY(), second.getX(), second.getY()))
                    return true;
            }
        }
        return false;
    }
}





















