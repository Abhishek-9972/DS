package DS.Backtracking.a06CombinationSum1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.com/problems/combination-sum/description/
 */
public class CombinationSum1 {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
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
                partial.add(input[i]);
                backtrack(input, partial, i, remain - input[i], list); // not i + 1 because we can reuse same elements
                partial.remove(partial.size() - 1);
            }
        }
    }
}

