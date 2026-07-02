package DS.Matrix.a10ZeroOneMatrix;

import java.util.LinkedList;
import java.util.Queue;

/**
 * https://leetcode.com/problems/01-matrix/
 */
public class ZeroOneMatrix {

    public int[][] updateMatrix(int[][] mat) {

        Queue<int[]> queue = new LinkedList<>();

        // Step 1 : Add all 0's to queue and mark all 1's as Infinity
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {

                if (mat[i][j] == 0) {
                    queue.offer(new int[]{i, j});
                } else {
                    mat[i][j] = Integer.MAX_VALUE;
                }
            }
        }

        int[][] directions = {
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1}
        };

        // Step 2 : Multi-Source BFS
        while (!queue.isEmpty()) {

            int[] current = queue.poll();

            int row = current[0];
            int col = current[1];

            for (int[] direction : directions) {

                int newRow = row + direction[0];
                int newCol = col + direction[1];

                if (newRow < 0 || newCol < 0 ||
                        newRow >= mat.length ||
                        newCol >= mat[0].length) {
                    continue;
                }

                // Found a shorter distance
                if (mat[row][col] + 1 < mat[newRow][newCol]) {

                    mat[newRow][newCol] = mat[row][col] + 1;

                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }

        return mat;
    }
}