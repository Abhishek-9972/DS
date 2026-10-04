package DS.Backtracking.a04Subset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.com/problems/subsets/description/
 */
public class Subset {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> list = new ArrayList<>();
        backtrack(0, nums, new ArrayList<>(), list);
        return list;
    }

    private void backtrack(int start, int[] input, List<Integer> partial, List<List<Integer>> list) {
        list.add(new ArrayList<>(partial));
        for (int i = start; i < input.length; i++) {
            partial.add(input[i]);
            backtrack(i + 1, input, partial, list);
            partial.remove(partial.size() - 1);
        }
    }
}
