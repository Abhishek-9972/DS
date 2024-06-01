package DS.DP.knapsack.unbounded.coinChange.coinChange1;

// This is also same as total number of ways to reach Nth Stair
public class CoinChange1 {
    public static void main(String[] args) {
        int set[] = {1, 2 ,3};
        int sum = 5;
        int n = set.length;
        CoinChange1 coinChange1 = new CoinChange1();
        int subset = coinChange1.findSubset(set, n, sum);
        System.out.println(subset);
    }

    private int findSubset(int[] arr, int n, int sum) {
        int dp[][] = new int[n + 1][sum + 1];

        // If sum is 0, then answer is true
        for (int i = 1; i <= sum; i++)
            dp[0][i] = 0;

        // If sum is not 0 and set is empty,
        // then answer is false
        for (int i = 0; i <= n; i++)
            dp[i][0] = 1;

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < sum + 1; j++) {
                if (arr[i - 1] <= j) {
                    dp[i][j] = dp[i][j - arr[i - 1]] + dp[i - 1][j];
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][sum];
    }
}
