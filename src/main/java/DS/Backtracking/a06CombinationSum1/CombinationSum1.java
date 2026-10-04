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
        backtrack(0, target, candidates, new ArrayList<>(), list);
        return list;
    }

    private void backtrack(int start,int remain,int[] input,List<Integer> partial,List<List<Integer>> list) {
        if (remain == 0) {
            list.add(new ArrayList<>(partial));
            return;
        }

        for (int i = start; i < input.length; i++) {
            if (input[i] > remain) {
                break;
            }
            partial.add(input[i]);
            // Use i, not i + 1, because elements can be reused
            backtrack(i, remain - input[i], input, partial, list);
            partial.remove(partial.size() - 1);
        }
    }
}