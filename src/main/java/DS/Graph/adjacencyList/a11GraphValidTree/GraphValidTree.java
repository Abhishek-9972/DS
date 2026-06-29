package DS.Graph.adjacencyList.a11GraphValidTree;

import java.util.ArrayList;
import java.util.List;

public class GraphValidTree {

    List<List<Integer>> graph = new ArrayList<>();
    boolean[] visited;

    public boolean validTree(int n, int[][] edges) {

        // A tree with n nodes must have exactly n-1 edges.
        if (edges.length != n - 1) {
            return false;
        }

        visited = new boolean[n];

        // Initialize adjacency list
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        // Build undirected graph
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];

            graph.get(u).add(v);
            graph.get(v).add(u);
        }

        // Check for cycle
        if (hasCycle(0, -1)) {
            return false;
        }

        // Check if graph is connected
        for (boolean nodeVisited : visited) {
            if (!nodeVisited) {
                return false;
            }
        }

        return true;
    }

    private boolean hasCycle(int node, int parent) {

        visited[node] = true;

        for (int neighbour : graph.get(node)) {

            if (!visited[neighbour]) {

                if (hasCycle(neighbour, node)) {
                    return true;
                }

            } else if (neighbour != parent) {

                // Visited neighbour that is not the parent
                // => Cycle found
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {

        GraphValidTree obj = new GraphValidTree();

        int n = 5;

        int[][] edges = {
                {0, 1},
                {0, 2},
                {0, 3},
                {1, 4}
        };

        System.out.println(obj.validTree(n, edges));
    }
}
