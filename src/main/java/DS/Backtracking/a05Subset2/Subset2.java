package DS.Backtracking.a05Subset2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.com/problems/subsets-ii/description/
 */
public class Subset2 {

    public List<List<Integer>> subsetsWithDup(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        backtrack(0, nums, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int[] nums, List<Integer> partial, List<List<Integer>> result) {
        result.add(new ArrayList<>(partial));
        for (int i = start; i < nums.length; i++) {
            // Skip duplicate choices at the same recursion level
            if (i > start && nums[i] == nums[i - 1]) {
                continue;
            }
            partial.add(nums[i]);
            backtrack(i + 1, nums, partial, result);
            partial.remove(partial.size() - 1);
        }
    }
}