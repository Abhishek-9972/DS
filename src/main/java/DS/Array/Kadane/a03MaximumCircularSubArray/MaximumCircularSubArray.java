package DS.Array.Kadane.a03MaximumCircularSubArray;

/**
 * https://leetcode.com/problems/maximum-sum-circular-subarray/description/
 */
public class MaximumCircularSubArray {

    // Function to find the maximum circular subarray sum
    public int maxSubarraySumCircular(int[] nums) {
        int size = nums.length;

        int[] maxDp = new int[size];
        int[] minDp = new int[size];

        maxDp[0] = nums[0];
        minDp[0] = nums[0];

        int max = maxDp[0];
        int min = minDp[0];
        int totalSum = nums[0];

        for (int i = 1; i < size; i++) {

            // Kadane's DP: maximum subarray sum ending at i
            maxDp[i] = Math.max(nums[i], nums[i] + maxDp[i - 1]);
            max = Math.max(max, maxDp[i]);

            // Minimum subarray sum ending at i
            minDp[i] = Math.min(nums[i], nums[i] + minDp[i - 1]);
            min = Math.min(min, minDp[i]);

            totalSum += nums[i];
        }

        // All elements are negative: do not allow an empty subarray
        if (max < 0) {
            return max;
        }

        // Circular sum = total sum - minimum subarray sum
        return Math.max(max, totalSum - min);
    }

    public static void main(String[] args) {
        int[] a = {5, -3, 5};

        MaximumCircularSubArray solution =
                new MaximumCircularSubArray();

        System.out.println(solution.maxSubarraySumCircular(a)); // 10
    }
}