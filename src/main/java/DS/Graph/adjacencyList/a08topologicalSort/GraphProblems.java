package DS.Graph.adjacencyList.a08topologicalSort;

public class GraphProblems {
    public static void main(String[] args) {
        int nodes = 6;

        Graph a = new Graph(nodes);

        a.addEdge(0, 1);
        a.addEdge(0, 2);
        a.addEdge(1, 3);
        a.addEdge(1, 4);
        a.addEdge(2, 4);
        a.addEdge(3, 5);
        a.addEdge(4, 5);

        System.out.println(a.topologicalSort());
    }
}