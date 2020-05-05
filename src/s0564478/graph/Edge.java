package s0564478.graph;

public class Edge<T> {
    private final Vertex<T> to;
    private final double weight;

    public Edge(Vertex<T> to, int weight) {
        this.to = to;
        this.weight = weight;
    }

    public Vertex<T> getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }
}
