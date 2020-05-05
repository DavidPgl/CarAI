package s0564478.graph;

import java.util.ArrayList;
import java.util.List;

public class Vertex<T> {
    private final T data;
    private final List<Edge<T>> edges;

    public Vertex(T data) {
        this.data = data;
        edges = new ArrayList<>();
    }

    public Edge<T> getEdgeTo(Vertex<T> vertex) {
        for (Edge<T> edge : edges) {
            if (edge.getTo().equals(vertex))
                return edge;
        }

        return null;
    }

    public T getData() {
        return data;
    }

    public List<Edge<T>> getEdges() {
        return edges;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vertex)
            return data.equals(((Vertex<?>) obj).data);
        else
            return false;
    }
}
