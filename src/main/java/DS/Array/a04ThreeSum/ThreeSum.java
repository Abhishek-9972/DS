package DS.Array.a04ThreeSum;

import java.util.*;

/**
 * https://leetcode.com/problems/3sum/description/
 *
 *
 */
public class ThreeSum {
    public List<List<Integer>> threeSum(int[] nums) {
        Set<List<Integer>> result = new LinkedHashSet<>();

        int l, r;

        Arrays.sort(nums);
        for (int i = 0; i < nums.length - 2; i++) {


            l = i + 1;
            r = nums.length - 1;
            while (l < r) {
                List<Integer> temp = new LinkedList<>();
                if (nums[i] + nums[l] + nums[r] == 0) {
                    temp.add(nums[i]);
                    temp.add(nums[l]);
                    temp.add(nums[r]);
                    result.add(temp);
                    while (l < r && nums[l] == nums[l + 1]) {
                        l++;
                    }
                    while (l < r && nums[r] == nums[r - 1]) {
                        r--;
                    }

                    l++;
                    r--;
                } else if (nums[i] + nums[l] + nums[r] < 0)
                    l++;

                else
                    r--;
            }
        }

        // If we reach here, then no triplet was found
        List<List<Integer>> output = new LinkedList<>(result);
        return output;
    }
}

