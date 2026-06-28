package DS.zArrayBkp.a43PrefixSum;

public class PrefixSum {

    private int[] prefix;

    public PrefixSum(int[] nums) {

        prefix = new int[nums.length];

        prefix[0] = nums[0];

        for (int i = 1; i < nums.length; i++) {
            prefix[i] = prefix[i - 1] + nums[i];
        }
    }

    public int rangeSum(int left, int right) {

        if (left == 0) {
            return prefix[right];
        }

        return prefix[right] - prefix[left - 1];
    }
}

