package ds.Array.a09SearchInRotatedSortedArray;

/**
 * https://leetcode.com/problems/search-in-rotated-sorted-array/description/
 */
public class SearchInRotatedSortedArray {
    public int search(int[] nums, int target) {
        int st = 0;
        int end = nums.length - 1;

        while (st <= end) {
            int mid = (st + end) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[st] <= nums[mid]) {
                if (nums[st] <= target && nums[mid] >= target) {
                    end = mid - 1;
                } else {
                    st = mid + 1;
                }
            } else {
                if (nums[mid] <= target && nums[end] >= target) {
                    st = mid + 1;
                } else {
                    end = mid - 1;
                }
            }
        }
        return -1;
    }
}
