package ds.Array.a20MinimumSubarrayLength;

/**
 * https://leetcode.com/problems/minimum-size-subarray-sum/description/
 *
 */
public class MinimumSubArrayLength {
    public int minSubArrayLen(int target, int[] nums) {
        int result = Integer.MAX_VALUE;
        int left = 0;
        int valSum = 0;

        for (int i = 0; i < nums.length; i++) {
            valSum = valSum + nums[i];

            while (valSum >= target) {
                result = Math.min(result, i + 1 - left);
                valSum = valSum - nums[left];
                left++;
            }
        }

        return result != Integer.MAX_VALUE ? result : 0;
    }
}

