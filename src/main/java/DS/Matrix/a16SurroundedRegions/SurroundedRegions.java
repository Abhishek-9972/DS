package DS.Matrix.a16SurroundedRegions;

/**
 * https://leetcode.com/problems/surrounded-regions/
 *
 * Pattern: Graph / DFS / Matrix
 * Difficulty: Medium
 * Time Complexity: O(m * n)
 * Space Complexity: O(m * n) in the worst case (recursion stack)
 */
public class SurroundedRegions {

    public void solve(char[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        // Step 1: Find safe 'O's connected to the boundary.
        for (int i = 0; i < rows; i++) {
            dfs(board, i, 0);          // First column
            dfs(board, i, cols - 1);   // Last column
        }

        for (int j = 0; j < cols; j++) {
            dfs(board, 0, j);          // First row
            dfs(board, rows - 1, j);   // Last row
        }

        // Step 2: Flip surrounded 'O's and restore safe cells.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 'O') {
                    board[i][j] = 'X';
                } else if (board[i][j] == 'T') {
                    board[i][j] = 'O';
                }
            }
        }
    }

    private void dfs(char[][] board, int row, int col) {
        int rows = board.length;
        int cols = board[0].length;

        // Stop if out of bounds or not an unvisited 'O'.
        if (row < 0 || row >= rows ||
                col < 0 || col >= cols ||
                board[row][col] != 'O') {
            return;
        }

        // Mark as safe so it will not be captured.
        board[row][col] = 'T';

        // Explore all four directions.
        dfs(board, row - 1, col); // Up
        dfs(board, row + 1, col); // Down
        dfs(board, row, col - 1); // Left
        dfs(board, row, col + 1); // Right
    }
}