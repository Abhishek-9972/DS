package DS.Backtracking.a07CombinationSum2;

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
        backtrack(0, target, candidates, new ArrayList<>(), list);
        return list;
    }

    private void backtrack(int start, int remain, int[] input, List<Integer> partial, List<List<Integer>> list) {

        if (remain == 0) {
            list.add(new ArrayList<>(partial));
            return;
        }

        for (int i = start; i < input.length; i++) {
            // Since array is sorted, no later element can fit
            if (input[i] > remain) {
                break;
            }
            // Skip duplicate choices at the same recursion level
            if (i > start && input[i] == input[i - 1]) {
                continue;
            }
            partial.add(input[i]);
            // i + 1 because each element can be used only once
            backtrack(i + 1, remain - input[i], input, partial, list);
            partial.remove(partial.size() - 1);
        }
    }
}