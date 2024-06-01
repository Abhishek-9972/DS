package DS.DP.MISC.a02LongestIncreasingSubsequence;

public class LongestIncreasingSubsequence {
    public int lengthOfLIS(int[] nums) {
        int[] lis = new int[nums.length];
        for (int i = 0; i < lis.length; i++) {
            lis[i] = 1;
        }

        int max = 1;

        for (int i = 1; i < lis.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[i] > nums[j] && 1 + lis[j] > lis[i]) {
                    lis[i] = 1 + lis[j];
                    max = Math.max(max, lis[i]);
                }
            }
        }

        return max;
    }
}
