package DS.Graph.adjacencyList.a07CourseSchedule;

import java.util.*;

/**
 * Same question as hasCycle in Graph
 */
public class CourseSchedule {
    List<List<Integer>> graph = new ArrayList<>();
    HashMap<Integer, Integer> incomingDegree = new HashMap<>();
    Queue<Integer> sourceQueue = new LinkedList<>();
    int visitedNodes = 0;
    int nodes;

    public boolean canFinish(int numCourses, int[][] prerequisites) {
        this.nodes = numCourses;
        for (int i = 0; i < nodes; i++) {
            graph.add(i, new ArrayList<>());
            incomingDegree.put(i, 0);
        }

        for(int i=0; i<prerequisites.length; i++){
            addEdge(prerequisites[i][0],prerequisites[i][1]);
        }

        return !checkIfGraphHasCycle();
    }

    public void addEdge(int a, int b) {
        graph.get(a).add(b);
        incomingDegree.put(b, incomingDegree.get(b) + 1);
    }

    public boolean checkIfGraphHasCycle() {

        for (Map.Entry<Integer, Integer> entry : incomingDegree.entrySet()) {
            if (entry.getValue() == 0) {
                sourceQueue.add(entry.getKey());
            }
        }

        while (!sourceQueue.isEmpty()) {
            visitedNodes++;
            int source = sourceQueue.remove();

            List<Integer> childrenList = graph.get(source);

            for (Integer child : childrenList) {
                incomingDegree.put(child, incomingDegree.get(child) - 1);

                if (incomingDegree.get(child) == 0) {
                    sourceQueue.add(child);
                }
            }
        }

        return nodes != visitedNodes;
    }
}