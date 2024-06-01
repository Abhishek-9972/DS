package DS.DP.knapsack.zeroByOne.countSubsetDiff;

public class CountSubsetDiff {
    public static void main(String[] args) {
        int[] arr = {1, 1, 2, 3};
        int diff = 1;
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum = sum + i;
        }
        int s1 = (sum + diff) /2;

        CountSubsetDiff countSubsetDiff = new CountSubsetDiff();
        int subset = countSubsetDiff.findSubset(arr, arr.length, s1);
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
                    dp[i][j] = dp[i - 1][j - arr[i - 1]] + dp[i - 1][j];
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][sum];
    }
}
