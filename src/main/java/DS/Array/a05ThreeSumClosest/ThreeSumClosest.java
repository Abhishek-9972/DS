package DS.Array.a05ThreeSumClosest;

import java.util.Arrays;

/**
 * https://leetcode.com/problems/3sum-closest/description/
 */
public class ThreeSumClosest {
        public int threeSumClosest(int[] nums, int target) {
            int l, r;
            Arrays.sort(nums);
            int closestSum = nums[0] + nums[1] + nums[2];

            for (int i = 0; i < nums.length - 2; i++) {

                l = i + 1;
                r = nums.length - 1;
                while (l < r) {
                    int sum = nums[i] + nums[l] + nums[r];

                    if (Math.abs(target - sum) < Math.abs(target - closestSum)) {
                        closestSum = sum; // Update closest sum if the current sum is closer to the target
                    }
                    else if (sum<target)
                        l++;
                    else // A[i] + A[l] + A[r] > sum
                        r--;
                }
            }

            return closestSum;
        }
    }

