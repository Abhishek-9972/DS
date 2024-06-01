package DS.Graph.adjacencyList.hasCycle.undirected;

public class GraphProblems {

    public static void main(String[] args) {
        int nodes = 3;

        Graph a = new Graph(nodes);

        a.addEdge(0, 1);
        a.addEdge(1, 2);
        a.addEdge(2, 0);

        System.out.println(a.ifUndirectedGraphHasCycle());
    }

}