package DS.Array.a10FirstAndLastPositionInSortedArray;

/**
 * https://leetcode.com/problems/search-in-rotated-sorted-array/description/
 */
public class FirstAndLastPositionInSortedArray {
    public int[] searchRange(int[] nums, int target) {
        int n = nums.length;
        int st = 0, end = n - 1;
        int first = -1;
        int last = -1;

        while (st <= end) {
            int mid = (st + end) / 2;
            if (target == nums[mid]) {
                first = mid;
                end = mid - 1;
            } else if (target > nums[mid]) {
                st = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        st = 0;
        end = n - 1;

        while (st <= end) {
            int mid = (st + end) / 2;
            if (target == nums[mid]) {
                last = mid;
                st = mid + 1;
            } else if (target > nums[mid]) {
                st = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        return new int[]{first, last};

    }
}

