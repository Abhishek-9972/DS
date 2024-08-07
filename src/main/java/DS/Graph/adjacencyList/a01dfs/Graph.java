package DS.Graph.adjacencyList.a01dfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Graph {
    List<List<Integer>> graph;
    boolean visited[];

    Graph(int nodes) {
        graph = new ArrayList<>();
        visited = new boolean[nodes];

        for (int i = 0; i < nodes; i++) {
            graph.add(i, new ArrayList<>());
        }
    }

    public void addEdge(int a, int b) {
        graph.get(a).add(b);
        graph.get(b).add(a);
    }

    public void dfs(int start) {
        Stack<Integer> stack = new Stack<>();

        stack.push(start);
        visited[start] = true;

        while (!stack.isEmpty()) {
            Integer node = stack.pop();
            System.out.print(node + " ");

            List<Integer> neighboursList = graph.get(node);

            for (Integer neighbour : neighboursList) {
                if (!visited[neighbour]) {
                    stack.push(neighbour);
                    visited[neighbour] = true;
                }
            }
        }
    }
}
