package DS.BinarySearch.a14LongestIncreasingSubsequence;

/**
 * Time Complexity O(nlogn)
 */
public class LongestIncreasingSubsequence {
    public int lengthOfLIS(int[] nums) {
        int[] tails = new int[nums.length];
        int size = 0;
        for (int num : nums) {
            int start = 0;
            int end = size;
            while (start < end) {
                int mid = start + (end - start) / 2;
                if (tails[mid] < num) {
                    start = mid + 1;
                } else {
                    end = mid;
                }
            }
            tails[start] = num;
            if (start == size) {
                size++;
            }
        }
        return size;
    }
}