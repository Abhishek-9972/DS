package DS.DP.MISC.a02LongestIncreasingSubsequence.Optimized;

public class LongestIncreasingSubsequence2 {

    public int lengthOfLIS(int[] nums) {

        if (nums.length == 0) {
            return 0;
        }

        int[] lis = new int[nums.length];
        int size = 0;

        for (int num : nums) {

            int start = 0;
            int end = size;

            // Binary search for the first element >= num
            while (start < end) {

                int mid = start + (end - start) / 2;

                if (lis[mid] < num) {
                    start = mid + 1;
                } else {
                    end = mid;
                }
            }

            lis[start] = num;

            if (start == size) {
                size++;
            }
        }

        return size;
    }
}