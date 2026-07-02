package DS.Matrix.a12ShortestPathInBinaryMatrix;

import java.util.LinkedList;
import java.util.Queue;

/**
 * https://leetcode.com/problems/shortest-path-in-binary-matrix/
 */
public class ShortestPathInBinaryMatrix {

    class Node {
        int row;
        int col;
        int distance;

        Node(int row, int col, int distance) {
            this.row = row;
            this.col = col;
            this.distance = distance;
        }
    }

    public int shortestPathBinaryMatrix(int[][] grid) {

        int rows = grid.length;
        int cols = grid[0].length;

        // Start or destination blocked
        if (grid[0][0] == 1 || grid[rows - 1][cols - 1] == 1) {
            return -1;
        }

        Queue<Node> queue = new LinkedList<>();

        // Step 1 : Start BFS from (0,0)
        queue.offer(new Node(0, 0, 1));
        grid[0][0] = 1;      // Mark visited

        int[][] directions = {
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1},
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        // Step 2 : BFS
        while (!queue.isEmpty()) {

            Node current = queue.poll();

            int row = current.row;
            int col = current.col;
            int distance = current.distance;

            // Destination reached
            if (row == rows - 1 && col == cols - 1) {
                return distance;
            }

            for (int[] direction : directions) {

                int newRow = row + direction[0];
                int newCol = col + direction[1];

                if (newRow < 0 || newCol < 0 ||
                        newRow >= rows || newCol >= cols ||
                        grid[newRow][newCol] != 0) {
                    continue;
                }

                // Visit neighbour
                grid[newRow][newCol] = 1;

                queue.offer(
                        new Node(
                                newRow,
                                newCol,
                                distance + 1
                        )
                );
            }
        }

        return -1;
    }
}