package DS.Graph.adjacencyList.a12NoOfConnectedComponentsUndirectedGraph;

import java.util.ArrayList;
import java.util.List;

public class NumberOfConnectedComponents {

    List<List<Integer>> graph = new ArrayList<>();
    boolean[] visited;

    public int countComponents(int n, int[][] edges) {

        visited = new boolean[n];

        // Initialize graph
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

        int components = 0;

        for (int i = 0; i < n; i++) {

            if (!visited[i]) {

                dfs(i);

                components++;
            }
        }

        return components;
    }

    private void dfs(int node) {

        visited[node] = true;

        for (int neighbour : graph.get(node)) {

            if (!visited[neighbour]) {
                dfs(neighbour);
            }
        }
    }

    public static void main(String[] args) {

        NumberOfConnectedComponents obj = new NumberOfConnectedComponents();

        int n = 5;

        int[][] edges = {
                {0,1},
                {1,2},
                {3,4}
        };

        System.out.println(obj.countComponents(n, edges));
    }
}