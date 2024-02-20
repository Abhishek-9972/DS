package ds.DP.knapsack.zeroByOne.zeroByOne;

import java.util.Arrays;

import static java.lang.Math.max;

public class MemoizationKnapsack {
    public static void main(String[] args) {
        int[] wt = {1, 3, 4, 5};
        int[] v = {1, 4, 5, 7};
        int w = 7;
        MemoizationKnapsack memoizationKnapsack = new MemoizationKnapsack();
        int[][] dp = new int[wt.length + 1][w + 1];
        for (int i = 0; i <= wt.length; i++) {
            Arrays.fill(dp[i], -1);
        }
        int knapsack = memoizationKnapsack.knapsack(wt, v, w, wt.length, dp);
        System.out.println("0/1 Memoization Knapsack : " + knapsack);
    }

    private int knapsack(int[] wt, int[] v, int w, int n, int[][] dp) {
        if (n == 0 || w == 0) {
            return 0;
        }

        if (dp[n][w] != -1) {
            return dp[n][w];
        }

        if (wt[n - 1] <= w) {
            dp[n][w] = max(v[n - 1] + knapsack(wt, v, w - wt[n - 1], n - 1, dp), knapsack(wt, v, w, n - 1, dp));
            return dp[n][w];
        } else {
            dp[n][w] = knapsack(wt, v, w, n - 1, dp);
            return dp[n][w];
        }
    }
}
