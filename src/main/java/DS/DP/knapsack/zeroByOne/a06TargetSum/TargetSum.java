package DS.DP.knapsack.zeroByOne.a06TargetSum;

public class TargetSum {

    public int findTargetSumWays(int[] nums, int target) {

        int totalSum = 0;

        for (int num : nums) {
            totalSum += num;
        }

        // Step 2: Target cannot be greater than total sum
        // Step 3: (totalSum + target) must be even
        if (Math.abs(target) > totalSum || (totalSum + target) % 2 != 0) {
            return 0;
        }

        /*
         * positiveSum - negativeSum = target
         * positiveSum + negativeSum = totalSum

         * positiveSum = (totalSum + target) / 2
         */
        int subsetSum = (totalSum + target) / 2;

        return countSubset(nums, subsetSum);
    }

    private int countSubset(int[] nums, int sum) {

        int n = nums.length;
        int[][] dp = new int[n + 1][sum + 1];

        for (int i = 0; i < n + 1; i++) {
            dp[i][0] = 1;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < sum + 1; j++) {
                if (nums[i - 1] <= j) {
                    dp[i][j] = dp[i - 1][j - nums[i - 1]] + dp[i - 1][j];
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }

        return dp[n][sum];
    }
}
