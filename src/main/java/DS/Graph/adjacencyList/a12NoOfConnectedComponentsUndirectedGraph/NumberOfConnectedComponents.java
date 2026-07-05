package DS.Graph.adjacencyList.a12NoOfConnectedComponentsUndirectedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/
 */
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

    private void dfs(int start) {

        Stack<Integer> stack = new Stack<>();

        stack.push(start);
        visited[start] = true;

        while (!stack.isEmpty()) {

            int node = stack.pop();

            for (int neighbour : graph.get(node)) {

                if (!visited[neighbour]) {

                    visited[neighbour] = true;
                    stack.push(neighbour);
                }
            }
        }
    }

    public static void main(String[] args) {

        NumberOfConnectedComponents obj = new NumberOfConnectedComponents();

        int n = 5;

        int[][] edges = {
                {0, 1},
                {1, 2},
                {3, 4}
        };

        System.out.println(obj.countComponents(n, edges)); // 2
    }
}