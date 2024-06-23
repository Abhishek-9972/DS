package DS.DP.MISC.a08HouseRobber02;

public class HouseRobber2 {

    public int rob(int[] nums) {
        if (nums.length < 2) {
            return nums[0];
        }

        int[] skipLastHouse = new int[nums.length - 1];
        int[] skipFirstHouse = new int[nums.length - 1];

        for (int i = 0; i < nums.length - 1; i++) {
            skipLastHouse[i] = nums[i];
            skipFirstHouse[i] = nums[i + 1];
        }

        int lootSkippingLast = robHouse(skipLastHouse);
        int lootSkippingFirst = robHouse(skipFirstHouse);

        return Math.max(lootSkippingLast, lootSkippingFirst);
    }

    public static int robHouse(int[] nums) {
        int n = nums.length;

        if (n == 1) {
            return nums[0];
        }

        int[] dp = new int[n];

        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);

        for (int i = 2; i < n; i++) {
            dp[i] = Math.max(dp[i - 1], nums[i] + dp[i - 2]);
        }

        return dp[n - 1];
    }
}