package ds.Array.a06FourSum;

import java.util.*;

/**
 * https://leetcode.com/problems/4sum/description/
 */
public class FourSum {
    public List<List<Integer>> fourSum(int[] nums, int target) {
        int n = nums.length;

        List<List<Integer>> ans = new ArrayList<>();
        Arrays.sort(nums);

        Set<List<Integer>> set = new HashSet<>();

        for (int i = 0; i < n - 2; i++) {
            for (int j = i + 1; j < n; j++) {
                int l = j + 1;
                int r = n - 1;

                while (l < r) {
                    long sum = (long) nums[i] + nums[j] + nums[l] + nums[r];

                    if (sum == target) {
                        List<Integer> al = new ArrayList<>();
                        al.add(nums[i]);
                        al.add(nums[j]);
                        al.add(nums[l]);
                        al.add(nums[r]);

                        set.add(al);
                        l++;
                        r--;
                    } else if (sum < target) l++;
                    else if (sum > target) r--;

                }
            }
        }

        ans.addAll(set);
        return ans;
    }
}

