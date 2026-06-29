package DS.Graph.adjacencyList.a07CourseSchedule;

import java.util.*;

public class CourseSchedule1 {

    private List<List<Integer>> graph = new ArrayList<>();
    private Map<Integer, Integer> incomingDegree = new HashMap<>();
    private Queue<Integer> sourceQueue = new LinkedList<>();

    private int visitedNodes = 0;
    private int nodes;

    public boolean canFinish(int numCourses, int[][] prerequisites) {

        this.nodes = numCourses;

        // Initialize graph and indegree
        for (int i = 0; i < nodes; i++) {
            graph.add(new ArrayList<>());
            incomingDegree.put(i, 0);
        }

        // Build graph
        // prerequisite -> course
        for (int[] prerequisite : prerequisites) {
            int course = prerequisite[0];
            int preReq = prerequisite[1];

            addEdge(preReq, course);
        }

        return !checkIfGraphHasCycle();
    }

    public void addEdge(int a, int b) {
        graph.get(a).add(b);
        incomingDegree.put(b, incomingDegree.get(b) + 1);
    }

    private boolean checkIfGraphHasCycle() {

        // Find all source nodes (indegree = 0)
        for (int i = 0; i < nodes; i++) {
            if (incomingDegree.get(i) == 0) {
                sourceQueue.add(i);
            }
        }

        while (!sourceQueue.isEmpty()) {

            int source = sourceQueue.poll();
            visitedNodes++;

            for (int child : graph.get(source)) {

                incomingDegree.put(child, incomingDegree.get(child) - 1);

                if (incomingDegree.get(child) == 0) {
                    sourceQueue.add(child);
                }
            }
        }

        // If all nodes were processed, graph has no cycle.
        return visitedNodes != nodes;
    }

    public static void main(String[] args) {

        ZCourseSchedule cs = new ZCourseSchedule();

        int numCourses = 4;

        int[][] prerequisites = {
                {1, 0},
                {2, 0},
                {3, 1},
                {3, 2}
        };

        System.out.println(cs.canFinish(numCourses, prerequisites));
    }
}