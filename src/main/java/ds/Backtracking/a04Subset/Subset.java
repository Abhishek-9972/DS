package ds.Backtracking.a04Subset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.com/problems/subsets/description/
 */
public class Subset {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> list = new ArrayList<>();
        Arrays.sort(nums);
        backtrack(nums, new ArrayList<>(), 0, list);
        return list;
    }

    private void backtrack(int[] input, List<Integer> partial, int start, List<List<Integer>> list) {
        list.add(new ArrayList<>(partial));
        for (int i = start; i < input.length; i++) {
            partial.add(input[i]);
            backtrack(input, partial, i + 1, list);
            partial.remove(partial.size() - 1);
        }
    }
}

