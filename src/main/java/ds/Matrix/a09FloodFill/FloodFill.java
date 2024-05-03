package ds.Matrix.a09FloodFill;

/**
 * https://leetcode.com/problems/flood-fill/description/
 */
public class FloodFill {
        public int[][] floodFill(int[][] image, int sr, int sc, int color) {
            int startColor = image[sr][sc];
            int rowsLastIndex = image.length - 1;
            int colsLastIndex = image[0].length - 1;

            if (startColor == color) {
                return image; // No need to fill if the start color is already the target color
            } else {
                recursiveFlood(image, sr, sc, color, startColor, rowsLastIndex, colsLastIndex);
                return image;
            }
        }

        private void recursiveFlood(int[][] image, int sr, int sc, int color, int startColor, int rowsLastIndex, int colsLastIndex) {
            if (sr < 0 || sr > rowsLastIndex || sc < 0 || sc > colsLastIndex || image[sr][sc] != startColor) {
                return; // Base case: pixel out of bounds or has different color
            }

            image[sr][sc] = color; // Change color of current pixel

            // Recursively call flood fill on adjacent pixels
            recursiveFlood(image, sr - 1, sc, color, startColor, rowsLastIndex, colsLastIndex); // up
            recursiveFlood(image, sr, sc + 1, color, startColor, rowsLastIndex, colsLastIndex); // right
            recursiveFlood(image, sr + 1, sc, color, startColor, rowsLastIndex, colsLastIndex); // down
            recursiveFlood(image, sr, sc - 1, color, startColor, rowsLastIndex, colsLastIndex); // left
        }

}
