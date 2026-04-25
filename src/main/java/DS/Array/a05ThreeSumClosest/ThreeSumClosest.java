package DS.Array.a05ThreeSumClosest;

import java.util.Arrays;

/**
 * https://leetcode.com/problems/3sum-closest/description/
 */
public class ThreeSumClosest {
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);

        int closest = nums[0] + nums[1] + nums[2];

        for (int i = 0; i < nums.length - 2; i++) {

            int l = i + 1;
            int r = nums.length - 1;

            while (l < r) {
                int sum = nums[i] + nums[l] + nums[r];

                // update closest
                if (Math.abs(sum - target) < Math.abs(closest - target)) {
                    closest = sum;
                }

                if (sum < target) {
                    l++;
                } else if (sum > target) {
                    r--;
                } else {
                    return sum; // exact match
                }
            }
        }

        return closest;
    }
    }

