package DS.Graph.adjacencyList.a07zCourseSchedule2;

import java.util.*;

/**
 * https://leetcode.com/problems/course-schedule-ii/description/
 */
public class CourseSchedule2 {

    List<List<Integer>> graph = new ArrayList<>();
    HashMap<Integer, Integer> incomingDegree = new HashMap<>();
    Queue<Integer> sourceQueue = new LinkedList<>();
    int visitedNodes = 0;
    int nodes;

    public int[] findOrder(int numCourses, int[][] prerequisites) {
        this.nodes = numCourses;
        for (int i = 0; i < nodes; i++) {
            graph.add(i, new ArrayList<>());
            incomingDegree.put(i, 0);
        }

        for(int i=0; i<prerequisites.length; i++){
            addEdge(prerequisites[i][0],prerequisites[i][1]);
        }

        List<Integer> result = checkIfGraphHasCycle();
        int [] arr = new int[result.size()];
        for(int i=0; i<result.size(); i++){
            arr[i] = result.get(i);
        }
        return arr;
    }

    public void addEdge(int a, int b) {
        graph.get(b).add(a);
        incomingDegree.put(a, incomingDegree.get(a) + 1);
    }

    public List<Integer> checkIfGraphHasCycle() {

        List<Integer> result = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : incomingDegree.entrySet()) {
            if (entry.getValue() == 0) {
                sourceQueue.add(entry.getKey());
            }
        }

        while (!sourceQueue.isEmpty()) {
            int source = sourceQueue.remove();
            result.add(source);

            List<Integer> childrenList = graph.get(source);

            for (Integer child : childrenList) {
                incomingDegree.put(child, incomingDegree.get(child) - 1);

                if (incomingDegree.get(child) == 0) {
                    sourceQueue.add(child);
                }
            }
        }

        if(nodes != result.size()){
            return new ArrayList<>();
        }

        return result;
    }
}
