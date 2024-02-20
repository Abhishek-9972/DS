package ds.DP.knapsack.unbounded.coinChange.coinChange2;

// This is also same as MinNoOfJumps To reach Nth Stair
public class MinNoOfCoins {

    public static void main(String[] args) {
        int set[] = {1, 2, 3};
        int sum = 5;
        int n = set.length;
        MinNoOfCoins minNoOfCoins = new MinNoOfCoins();
        int subset = minNoOfCoins.findSubset(set, n, sum);
        System.out.println(subset);
    }

    private int findSubset(int[] arr, int n, int sum) {
        int dp[][] = new int[n + 1][sum + 1];

        // If sum is 0, then answer is true
        for (int i = 0; i <= sum; i++)
            dp[0][i] = Integer.MAX_VALUE-1;

        // If sum is not 0 and set is empty,
        // then answer is false
        for (int i = 1; i <= n; i++)
            dp[i][0] = 0;

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < sum + 1; j++) {
                if (arr[i - 1] <= j) {
                    dp[i][j] = Math.min(1 + dp[i][j - arr[i - 1]], dp[i - 1][j]);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][sum];
    }
}