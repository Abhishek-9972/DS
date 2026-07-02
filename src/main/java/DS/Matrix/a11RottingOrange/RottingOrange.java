package DS.Matrix.a11RottingOrange;

import java.util.LinkedList;
import java.util.Queue;

/**
 * https://leetcode.com/problems/rotting-oranges/
 */
public class RottingOrange {

    public int orangesRotting(int[][] grid) {

        Queue<int[]> queue = new LinkedList<>();
        int fresh = 0;

        // Count fresh oranges and add all rotten oranges to queue
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {

                if (grid[i][j] == 1) {
                    fresh++;
                } else if (grid[i][j] == 2) {
                    queue.offer(new int[]{i, j});
                }
            }
        }

        // No fresh oranges
        if (fresh == 0) {
            return 0;
        }

        int minutes = 0;

        int[][] directions = {
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1}
        };

        while (!queue.isEmpty() && fresh > 0) {

            int size = queue.size();

            // Process one minute
            for (int i = 0; i < size; i++) {

                int[] current = queue.poll();

                int row = current[0];
                int col = current[1];

                for (int[] direction : directions) {

                    int newRow = row + direction[0];
                    int newCol = col + direction[1];

                    if (newRow < 0 || newCol < 0 ||
                            newRow >= grid.length ||
                            newCol >= grid[0].length ||
                            grid[newRow][newCol] != 1) {
                        continue;
                    }

                    // Rotten the fresh orange
                    grid[newRow][newCol] = 2;
                    fresh--;

                    queue.offer(new int[]{newRow, newCol});
                }
            }

            minutes++;
        }

        return fresh == 0 ? minutes : -1;
    }
}