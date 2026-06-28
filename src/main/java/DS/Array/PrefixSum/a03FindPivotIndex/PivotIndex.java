package DS.Array.PrefixSum.a03FindPivotIndex;

/**
 * https://leetcode.com/problems/find-pivot-index/
 *
 *  * Time Complexity: O(n)
 *  * Space Complexity: O(1)
 */
public class PivotIndex {

    public int pivotIndex(int[] nums) {
        int totalSum = 0;

        for (int num : nums) {
            totalSum += num;
        }

        int leftSum = 0;
        for (int i = 0; i < nums.length; i++) {
            int rightSum = totalSum - leftSum - nums[i];
            if (leftSum == rightSum) {
                return i;
            }
            leftSum += nums[i];
        }

        return -1;
    }
}
