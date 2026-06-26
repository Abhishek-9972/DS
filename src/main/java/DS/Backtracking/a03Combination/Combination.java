package DS.Backtracking.a03Combination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.com/problems/combinations/description/
 */
public class Combination {

    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(1, n, k, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int n, int k,
                           List<Integer> partial,
                           List<List<Integer>> result) {

        if (partial.size() == k) {
            result.add(new ArrayList<>(partial));
            return;
        }

        for (int i = start; i <= n; i++) {
            partial.add(i);
            backtrack(i + 1, n, k, partial, result);
            partial.remove(partial.size() - 1);
        }
    }
}
