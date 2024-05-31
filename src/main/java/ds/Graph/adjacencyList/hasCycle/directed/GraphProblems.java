package ds.Graph.adjacencyList.hasCycle.directed;

public class GraphProblems {

    public static void main(String[] args) {
        int nodes = 4;

        Graph a = new Graph(nodes);

        a.addEdge(0, 1);
        a.addEdge(1, 2);
        a.addEdge(2, 0);
        a.addEdge(3, 0);

        System.out.println(a.checkIfGraphHasCycle());
    }
}