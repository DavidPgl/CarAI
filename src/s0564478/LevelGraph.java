package s0564478;

import org.lwjgl.util.vector.Vector2f;
import s0564478.graph.Graph;
import s0564478.graph.Vertex;
import s0564478.util.VectorUtil;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class LevelGraph {
    private Graph<Point> graph;

    public LevelGraph(Polygon[] obstacles) {
        graph = new Graph<>();
        for (Polygon obstacle : obstacles) {
            for (int i = 0; i < obstacle.npoints; i++) {
                Point previous = new Point(obstacle.xpoints[i], obstacle.ypoints[i]);
                Point current = new Point(obstacle.xpoints[(i + 1) % obstacle.npoints], obstacle.ypoints[(i + 1) % obstacle.npoints]);
                Point next = new Point(obstacle.xpoints[(i + 2) % obstacle.npoints], obstacle.ypoints[(i + 2) % obstacle.npoints]);

                Vector2f a = VectorUtil.vectorFromPoints(current, previous);
                Vector2f b = VectorUtil.vectorFromPoints(current, next);

                // If angle is clockwise (negative) the corner points outwards.
                boolean cornerIsConvex = VectorUtil.angleIsClockwise(a, b);
                if (cornerIsConvex)
                    graph.add(current);
            }
        }
    }

    public List<Point> getConvexPoints() {
        return graph.getVertices().stream().map(Vertex::getData).collect(Collectors.toList());
    }
}
