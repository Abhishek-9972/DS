package DS.Array.Kadane.a02MaximumSubArray;

public class KadaneUsingDP {

    // Function to find the largest contiguous array sum
    public int maxSubArray(int[] nums) {
        int size = nums.length;
        int[] dp = new int[size];
        dp[0] = nums[0];
        int max = dp[0];
        for (int i = 1; i < size; i++) {
            dp[i] = Math.max(nums[i], nums[i] + dp[i - 1]);
            max = Math.max(max, dp[i]);
        }
        return max;
    }

    public static void main(String[] args) {
        int[] a = {-2, -3, 4, -1, -2, 1, 5, -3};
        KadaneUsingDP kadaneUsingDP = new KadaneUsingDP();
        kadaneUsingDP.maxSubArray(a); // Call the function to find and print the maximum contiguous array sum
    }
}