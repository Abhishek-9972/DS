package ds.DP.knapsack.zeroByOne.zeroByOne;

public class KnapsackTabulation {
    public static void main(String[] args) {
        KnapsackTabulation knapsackTabulation = new KnapsackTabulation();
        int[] wt = {1, 3, 4, 5};
        int[] v = {1, 4, 5, 7};
        int w = 7;
        int knapsack = knapsackTabulation.knapsack(wt, v, w, wt.length);
        System.out.println("Tabulation Knapsack : "+knapsack);
    }

    private int knapsack(int[] wt, int[] v, int w, int n) {
        int[][] dp = new int[n + 1][w + 1];
        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < w + 1; j++) {
                if (wt[i - 1] <= j) {
                    dp[i][j] = Math.max(v[i - 1] + dp[i - 1][j - wt[i - 1]], dp[i - 1][j]);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][w];
    }
}
