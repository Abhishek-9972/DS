package ds.Backtracking.a07CombinationSum2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.com/problems/combination-sum-ii/description/
 */
public class CombinationSum2 {
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> list = new ArrayList<>();
        Arrays.sort(candidates);
        backtrack(candidates, new ArrayList<>(), 0, target, list);
        return list;
    }

    private void backtrack(int[] input, List<Integer> partial, int start, int remain, List<List<Integer>> list) {

        if (remain < 0) return;
        else if (remain == 0) list.add(new ArrayList<>(partial));
        else {
            for (int i = start; i < input.length; i++) {
                if (i > start && input[i] == input[i - 1]) continue; // skip duplicates
                partial.add(input[i]);
                backtrack(input, partial, i + 1, remain - input[i], list); // not i + 1 because we can reuse same elements
                partial.remove(partial.size() - 1);
            }
        }
    }
}

