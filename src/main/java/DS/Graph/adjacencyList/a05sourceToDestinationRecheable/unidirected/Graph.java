package DS.Graph.adjacencyList.a05sourceToDestinationRecheable.unidirected;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Graph {
    List<List<Integer>> graph;
    boolean visited[];
    int nodes;

    Graph(int nodes) {
        graph = new ArrayList<>();
        visited = new boolean[nodes];
        this.nodes = nodes;

        for (int i = 0; i < nodes; i++) {
            graph.add(i, new ArrayList<>());
        }
    }

    public void addEdge(int a, int b) {
        graph.get(a).add(b);
        graph.get(b).add(a);
    }

    public boolean ifSourceConnectedToDestination(int source, int destination) {

        dfs(source);

        return visited[destination];
    }

    public void dfs(int start) {
        Stack<Integer> stack = new Stack<>();

        stack.push(start);
        visited[start] = true;

        while (!stack.isEmpty()) {
            Integer node = stack.pop();

            List<Integer> neighboursList = graph.get(node);

            for (Integer neighbour : neighboursList) {
                if (!visited[neighbour]) {
                    stack.push(neighbour);
                    visited[neighbour] = true;
                }
            }
        }
    }

    public void dfsAnother(int start) {
        visited[start] = true;

        List<Integer> neighboursList = graph.get(start);

        for (Integer neighbour : neighboursList) {
            if (!visited[neighbour]) {
                dfsAnother(neighbour);
            }
        }
    }
}