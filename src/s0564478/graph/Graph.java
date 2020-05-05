package s0564478.graph;

import java.util.*;

public class Graph<T> {

    private final List<Vertex<T>> vertices = new ArrayList<>();

    public boolean add(T vertexData) {
        if (this.contains(vertexData))
            return false;

        vertices.add(new Vertex<>(vertexData));
        return true;
    }

    public boolean addEdge(T from, T to, int weight) {
        Vertex<T> vertexFrom = getVertexFromData(from);
        Vertex<T> vertexTo = getVertexFromData(to);

        if (vertexFrom != null && vertexTo != null) {
            vertexFrom.getEdges().add(new Edge<>(vertexTo, weight));
            return true;
        }
        return false;
    }

    public List<Vertex<T>> getVertices() {
        return vertices;
    }

    public List<Vertex<T>> getCheapestPath(T from, T to) {
        List<Vertex<T>> queue = new ArrayList<>();
        Map<Vertex<T>, Double> minWeights = new HashMap<>();
        Map<Vertex<T>, Vertex<T>> predecessor = new HashMap<>();
        List<Vertex<T>> connectedVertices = new ArrayList<>();

        Vertex<T> vertexFrom = getVertexFromData(from);
        Vertex<T> vertexTo = getVertexFromData(to);

        if (vertexFrom == null || vertexTo == null)
            return null;

        vertices.forEach((vertex) -> minWeights.put(vertex, Double.POSITIVE_INFINITY));

        minWeights.put(vertexFrom, 0.0);
        queue.add(vertexFrom);

        while (!queue.isEmpty()) {
            Vertex<T> currentVertex = queue.get(0);

            // Get cheapest vertex (to reach)
            for (Vertex<T> nextVertex : queue) {
                if (minWeights.get(nextVertex) < minWeights.get(currentVertex)) {
                    currentVertex = nextVertex;
                }
            }
            queue.remove(currentVertex);

            // Find cheapest weight
            for (Edge<T> edge : currentVertex.getEdges()) {
                Vertex<T> nextVertex = edge.getTo();
                double weightToNextVertex = minWeights.get(currentVertex) + edge.getWeight();

                if (weightToNextVertex < minWeights.get(nextVertex)) {
                    minWeights.put(nextVertex, weightToNextVertex);
                    predecessor.put(nextVertex, currentVertex);
                    connectedVertices.add(nextVertex);
                    queue.add(nextVertex);
                }
            }
        }


        // Check if we actually reached the goal
        if (!connectedVertices.contains(vertexTo))
            return null;

        List<Vertex<T>> path = new ArrayList<>();

        // Backtrack path from goal to start
        path.add(vertexTo);
        while (vertexTo != vertexFrom) {
            vertexTo = predecessor.get(vertexTo);
            path.add(vertexTo);
        }

        Collections.reverse(path);
        return path;
    }

    private Vertex<T> getVertexFromData(T vertexData) {
        for (Vertex<T> vertex : vertices)
            if (vertex.getData().equals(vertexData))
                return vertex;

        return null;
    }

    private boolean contains(T vertexData) {
        for (Vertex<T> vertex : vertices)
            if (vertex.getData().equals(vertexData))
                return true;

        return false;
    }

}
