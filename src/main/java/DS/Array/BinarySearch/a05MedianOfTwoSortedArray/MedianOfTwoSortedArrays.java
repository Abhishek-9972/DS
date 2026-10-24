package DS.Array.BinarySearch.a05MedianOfTwoSortedArray;

/**
 * https://leetcode.com/problems/median-of-two-sorted-arrays/description/
 */
public class MedianOfTwoSortedArrays {

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {

        // Always binary search on the smaller array
        if (nums1.length > nums2.length) {
            return findMedianSortedArrays(nums2, nums1);
        }

        int m = nums1.length;
        int n = nums2.length;

        int start = 0;
        int end = m;

        while (start <= end) {

            // Number of elements taken from nums1
            int partition1 = start + (end - start) / 2;

            // Number of elements taken from nums2
            int partition2 = (m + n + 1) / 2 - partition1;

            // Elements immediately around the partitions
            int left1 = partition1 == 0
                    ? Integer.MIN_VALUE
                    : nums1[partition1 - 1];

            int right1 = partition1 == m
                    ? Integer.MAX_VALUE
                    : nums1[partition1];

            int left2 = partition2 == 0
                    ? Integer.MIN_VALUE
                    : nums2[partition2 - 1];

            int right2 = partition2 == n
                    ? Integer.MAX_VALUE
                    : nums2[partition2];

            // Correct partition
            if (left1 <= right2 && left2 <= right1) {

                // Total number of elements is odd
                if ((m + n) % 2 == 1) {
                    return Math.max(left1, left2);
                }

                // Total number of elements is even
                int leftMax = Math.max(left1, left2);
                int rightMin = Math.min(right1, right2);

                return (leftMax + rightMin) / 2.0;
            }

            // nums1 partition is too far right
            if (left1 > right2) {
                end = partition1 - 1;
            }

            // nums1 partition is too far left
            else {
                start = partition1 + 1;
            }
        }

        return 0.0;
    }
}