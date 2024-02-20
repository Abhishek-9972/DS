package ds.DP.knapsack.unbounded.unbounded;


public class UnboundedKnapsack {
    public static void main(String[] args) {
        UnboundedKnapsack unboundedKnapsack = new UnboundedKnapsack();
        int[] wt = {1, 3, 4, 5};
        int[] v = {2, 4, 5, 7};
        int w = 7;
        int knapsack = unboundedKnapsack.knapsack(wt, v, w, wt.length);
        System.out.println("Tabulation unboundedKnapsack : " + knapsack);
    }

    private int knapsack(int[] wt, int[] v, int w, int n) {
        int[][] dp = new int[n + 1][w + 1];
        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < w + 1; j++) {
                if (wt[i - 1] <= j) {
                    dp[i][j] = Math.max(v[i - 1] + dp[i][j - wt[i - 1]], dp[i - 1][j]);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][w];
    }
}
