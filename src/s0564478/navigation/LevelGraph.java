package s0564478.navigation;

import lenz.htw.ai4g.track.Track;
import org.lwjgl.util.vector.Vector2f;
import s0564478.CarAI;
import s0564478.graph.Graph;
import s0564478.graph.OffsetPolygon;
import s0564478.graph.Vertex;
import s0564478.util.GLUtil;
import s0564478.util.VectorUtil;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * How we planned everything (BTS):
 * <a href="https://gyazo.com/e69bd83f009b11c91f1a40795e3203e2">Best sketch ever:P</a>
 */
public class LevelGraph {
    private static final int OBSTACLE_OFFSET = 20;
    private static final int ZONE_OFFSET = -20;
    private static final int AREA_POINT_INTERVAL = 12;
    private static final float SLOW_AREA_WEIGHT_FACTOR = 4;
    private static final float FAST_AREA_WEIGHT_FACTOR = 0.5f;

    private final Graph<LevelPoint> graph = new Graph<>();
    private final OffsetPolygon[] offsetObstacles;
    private final Track track;

    private final CarAI ai;
    private final OffsetPolygon[] offsetFastZones;
    private final OffsetPolygon[] offsetSlowZones;

    public LevelGraph(Track track, CarAI ai) {
        this.track = track;
        this.ai = ai;
        this.offsetObstacles = generateOffsetPolygons(track.getObstacles(), OBSTACLE_OFFSET);

        offsetFastZones = generateOffsetPolygons(track.getFastZones(), ZONE_OFFSET);
        offsetSlowZones = generateOffsetPolygons(track.getSlowZones(), ZONE_OFFSET);

        addObstacleVerticesToGraph(offsetObstacles);
        addSpecialZoneVerticesToGraph(offsetFastZones, LevelZone.ZoneType.FAST_ZONE);
        addSpecialZoneVerticesToGraph(offsetSlowZones, LevelZone.ZoneType.SLOW_ZONE);
        addFreeEdges();
    }

    public void updateCarAndCP(LevelPoint carPosition, LevelPoint checkpointPosition) {
        Vertex<LevelPoint> point = graph.add(carPosition);
        addFreeEdgesForVertex(point);
        addFreeEdgesForVertex(graph.add(checkpointPosition));


    }

    public List<Vertex<LevelPoint>> getVertices() {
        return graph.getVertices();
    }

    public List<Point> getVerticesPoints() {
        return graph.getVertices().stream().map(Vertex::getData).collect(Collectors.toList());
    }

    /**
     * Gets the cheapest path and initializes {@link LevelPoint}s for each point in the path.
     *
     * @param start Start point of path.
     * @param goal  End point of path.
     * @return List of {@link LevelPoint}s representing the path.
     */
    public List<LevelPoint> getPath(LevelPoint start, LevelPoint goal) {
        List<Vertex<LevelPoint>> cheapestPath = graph.getCheapestPath(start, goal);

        Vertex<LevelPoint> point = cheapestPath.get(0);
        ai.addDebugAction(() -> {
            point.getEdges().forEach(edge -> GLUtil.drawLine(point.getData(), edge.getTo().getData(), Color.GREEN));
        });

        List<Point> tempPath = cheapestPath.stream()
                .map(Vertex::getData)
                .collect(Collectors.toList());

        List<LevelPoint> path = new ArrayList<>();
        // Add start point
        path.add(new LevelPoint(tempPath.get(0), LevelPoint.Type.CHECKPOINT));
        for (int i = 1; i < tempPath.size() - 1; i++) {
            Point previousPoint = tempPath.get(i - 1);
            LevelPoint currentPoint = new LevelPoint(tempPath.get(i), LevelPoint.Type.ROUTE_POINT);
            Point nextPoint = tempPath.get(i + 1);

            // Calculate angle to next point
            Vector2f a = VectorUtil.vectorFromPoints(currentPoint, previousPoint).normalise(null);
            Vector2f b = VectorUtil.vectorFromPoints(currentPoint, nextPoint).normalise(null);
            currentPoint.setAngleToNextPoint(Vector2f.angle(a, b));

            // Add to new list
            path.add(currentPoint);
        }
        // Add checkpoint/goal
        path.add(new LevelPoint(tempPath.get(tempPath.size() - 1), LevelPoint.Type.CHECKPOINT));
        return path;
    }

    /**
     * Goes through the polygon list and applies an offset ({@link #OBSTACLE_OFFSET}) to it
     *
     * @param polygons All polygons that will be translated.
     */
    private OffsetPolygon[] generateOffsetPolygons(Polygon[] polygons, float offset) {
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
                if (!VectorUtil.angleIsClockwise(a, b))
                    continue;

                // Remove corner point if in special area
                if (isPointInSpecialArea(current))
                    continue;

                graph.add(new LevelPoint(current, LevelPoint.Type.ROUTE_POINT));
            }
        }
    }

    private void addFreeEdges() {
        for (Vertex<LevelPoint> currentVertex : graph.getVertices()) {
            addFreeEdgesForVertex(currentVertex);
        }
    }

    private void addFreeEdgesForVertex(Vertex<LevelPoint> currentVertex) {
        for (Vertex<LevelPoint> nextVertex : graph.getVertices()) {
            // Skip same Vertex
            if (nextVertex.equals(currentVertex))
                continue;

            // If any obstacle is intersected, skip
            if (checkForIntersection(currentVertex.getData(), nextVertex.getData(), offsetObstacles, -3f))
                continue;

            if (checkForIntersection(currentVertex.getData(), nextVertex.getData(), offsetSlowZones, -1.1f) ||
                    checkForIntersection(currentVertex.getData(), nextVertex.getData(), offsetFastZones, -1.1f)) {
                if ((currentVertex.getData().levelZone == null && nextVertex.getData().levelZone == null))
                    continue;
                if (currentVertex.getData().levelZone != nextVertex.getData().levelZone)
                    continue;
            }


            LevelPoint currentLevelPoint = currentVertex.getData();
            double weight = currentVertex.getData().distance(nextVertex.getData());
            if (currentLevelPoint.levelZone != null && currentLevelPoint.levelZone == nextVertex.getData().levelZone) {
                if (currentLevelPoint.levelZone.zoneType == LevelZone.ZoneType.FAST_ZONE)
                    weight = weight * FAST_AREA_WEIGHT_FACTOR;
                else if (currentLevelPoint.levelZone.zoneType == LevelZone.ZoneType.SLOW_ZONE)
                    weight = weight * SLOW_AREA_WEIGHT_FACTOR;
            }
            graph.addEdge(currentVertex.getData(), nextVertex.getData(), (int) weight);
            graph.addEdge(nextVertex.getData(), currentVertex.getData(), (int) weight);
        }
    }

    private boolean checkForIntersection(Point first, Point second, OffsetPolygon[] offsetPolygons, float offsetFactor) {
        for (OffsetPolygon polygon : offsetPolygons) {
            for (int i = 0; i < polygon.npoints; i++) {
                Point a = new Point(polygon.xpoints[i], polygon.ypoints[i]);
                int nextI = (i + 1) % polygon.npoints;
                Point b = new Point(polygon.xpoints[nextI], polygon.ypoints[nextI]);

                // Move points a bit to prevent unintended intersections with obstacles (at their position)
                a = VectorUtil.translatePoint(a, VectorUtil.scale(polygon.getNormalVectors()[i], offsetFactor));
                b = VectorUtil.translatePoint(b, VectorUtil.scale(polygon.getNormalVectors()[nextI], offsetFactor));

                if (Line2D.linesIntersect(a.getX(), a.getY(), b.getX(), b.getY(),
                        first.getX(), first.getY(), second.getX(), second.getY()))
                    return true;
            }
        }
        return false;
    }

    private void addSpecialZoneVerticesToGraph(Polygon[] polygons, LevelZone.ZoneType zoneType) {
        for (Polygon polygon : polygons) {
            for (int i = 0; i < polygon.npoints; i++) {
                Point current = new Point(polygon.xpoints[i], polygon.ypoints[i]);
                Point next = new Point(polygon.xpoints[(i + 1) % polygon.npoints], polygon.ypoints[(i + 1) % polygon.npoints]);

                LevelZone area = new LevelZone(zoneType);
                int numberOfPoints = (int) (current.distance(next) / AREA_POINT_INTERVAL);
                Vector2f offset = VectorUtil.scale(VectorUtil.vectorFromPoints(current, next).normalise(null), AREA_POINT_INTERVAL);
                for (int j = 0; j < numberOfPoints; j++) {
                    Point p = new Point(current.x + (int) (offset.x * j), current.y + (int) (offset.y * j));
                    LevelPoint levelPoint = new LevelPoint(p, LevelPoint.Type.ROUTE_POINT, area);
                    graph.add(levelPoint);
                }
            }
        }
    }

    private boolean isPointInSpecialArea(Point point) {
        for (Polygon fastZone : track.getFastZones())
            if (fastZone.contains(point))
                return true;

        for (Polygon slowZone : track.getSlowZones())
            if (slowZone.contains(point))
                return true;

        return false;
    }
}





















