package DS.Backtracking.a09GenerateParanthesis;

import java.util.ArrayList;
import java.util.List;

/**
 * https://leetcode.com/problems/generate-parentheses/
 *
 * Pattern: Backtracking
 *
 * Difficulty: Medium
 *
 * Time Complexity: O(4^n / √n)
 * Space Complexity: O(n) (excluding output)
 */
public class GenerateParenthesis {

    public List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        backtrack(0, 0, n, new StringBuilder(), result);
        return result;
    }

    private void backtrack(int left, int right, int n, StringBuilder partial, List<String> result) {

        if (partial.length() == 2 * n) {
            result.add(partial.toString());
            return;
        }

        if (left < n) {
            partial.append('(');
            backtrack(left + 1, right, n, partial, result);
            partial.deleteCharAt(partial.length() - 1);
        }

        if (right < left) {
            partial.append(')');
            backtrack(left, right + 1, n, partial, result);
            partial.deleteCharAt(partial.length() - 1);
        }
    }
}