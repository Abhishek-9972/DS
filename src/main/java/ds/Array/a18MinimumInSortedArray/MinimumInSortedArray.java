package ds.Array.a18MinimumInSortedArray;

/**
 * https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/description/
 */
public class MinimumInSortedArray {
    public int findMin(int[] nums) {
        int st = 0;
        int end = nums.length - 1;

        while (st < end) {
            int mid = (st + end) / 2;
            if (nums[mid] < nums[end]) {
                end = mid;
            } else {
                st = mid + 1;
            }
        }
        return nums[st];
    }
}

