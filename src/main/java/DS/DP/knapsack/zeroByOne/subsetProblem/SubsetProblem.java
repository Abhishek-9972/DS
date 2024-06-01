package DS.DP.knapsack.zeroByOne.subsetProblem;

public class SubsetProblem {

    public static void main(String[] args) {
        int set[] = {3, 34, 4, 12, 5, 2};
        int sum = 9;
        int n = set.length;
        SubsetProblem subsetProblem = new SubsetProblem();
        boolean subset = subsetProblem.findSubset(set, n, sum);
        System.out.println(subset);
    }

    private boolean findSubset(int[] arr, int n, int sum) {
        boolean dp[][] = new boolean[n + 1][sum + 1];

        // If sum is 0, then answer is true
        for (int i = 1; i <= sum; i++)
            dp[0][i] = false;

        // If sum is not 0 and set is empty,
        // then answer is false
        for (int i = 0; i <= n; i++)
            dp[i][0] = true;

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < sum + 1; j++) {
                if (arr[i - 1] <= j) {
                    dp[i][j] = dp[i - 1][j - arr[i - 1]] || dp[i - 1][j];
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][sum];
    }
}
