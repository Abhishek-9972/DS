package DS.DP.MISC.a08MinimumPathSum;

/**
 * https://www.youtube.com/watch?v=bMdX8muZsX8&list=PLFKexf2NBjmNNEsaJai9HCPh-26iSChZU&index=17
 */
public class MinimumPathSum {

    public static void main(String[] args) {
        MinimumPathSum mps = new MinimumPathSum();
        int[][] grid = {{1, 3, 2, 4},
                {2, 1, 3, 2},
                {5, 4, 2, 3},
                {4, 1, 1, 2}};
        int result = mps.minPathSum(grid);
        System.out.println("Min Path Sum : " + result);
    }

    public int minPathSum(int[][] grid) {

        int m = grid.length;
        int n = grid[0].length;

        int[][] dp = new int[m][n];

        dp[0][0] = grid[0][0];

        // First column
        for (int i = 1; i < m; i++) {
            dp[i][0] = dp[i - 1][0] + grid[i][0];
        }

        // First row
        for (int j = 1; j < n; j++) {
            dp[0][j] = dp[0][j - 1] + grid[0][j];
        }

        // Remaining cells
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = Math.min(dp[i - 1][j], dp[i][j - 1]) + grid[i][j];
            }
        }

        return dp[m - 1][n - 1];
    }
}