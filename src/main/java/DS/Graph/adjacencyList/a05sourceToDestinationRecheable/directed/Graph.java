package DS.Graph.adjacencyList.a05sourceToDestinationRecheable.directed;

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
    }

    public boolean ifSourceConnectedToDestination(int source, int destination) {

        dfs(source);

        return visited[destination];
    }

    public void dfs(int index) {
        Stack<Integer> stack = new Stack<>();

        stack.push(index);
        visited[index] = true;

        while (!stack.isEmpty()) {
            Integer nodeIndex = stack.pop();
            visited[nodeIndex] = true;

            List<Integer> neighboursList = graph.get(nodeIndex);

            for (Integer neighbour : neighboursList) {
                if (!visited[neighbour]) {
                    stack.push(neighbour);
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