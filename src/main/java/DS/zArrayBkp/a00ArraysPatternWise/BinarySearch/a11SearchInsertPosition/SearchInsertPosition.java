package DS.zArrayBkp.a00ArraysPatternWise.BinarySearch.a11SearchInsertPosition;

/**
 * https://leetcode.com/problems/search-insert-position/description/
 *
 *  * Time Complexity: O(log n)
 *  * Space Complexity: O(1)
 */
public class SearchInsertPosition {
    public int searchInsert(int[] nums, int target) {
        int st = 0;
        int end = nums.length - 1;

        while (st <= end) {
            int mid = (st + end) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (target > nums[mid]) {
                st = mid + 1;
            } else {
                end = mid - 1;
            }
        }
        return st;
    }
}

