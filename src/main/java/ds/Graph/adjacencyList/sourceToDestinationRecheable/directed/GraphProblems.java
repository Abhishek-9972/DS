package ds.Graph.adjacencyList.sourceToDestinationRecheable.directed;

public class GraphProblems {

    public static void main(String[] args) {
        int nodes = 5;

        Graph a = new Graph(nodes);

        a.addEdge(0, 1);
        a.addEdge(1, 2);
        a.addEdge(2, 0);
        a.addEdge(3, 2);
        a.addEdge(3, 0);
        a.addEdge(2, 4);

        System.out.println(a.ifSourceConnectedToDestination(0, 3));
        System.out.println(a.ifSourceConnectedToDestination(0, 2));
    }

}