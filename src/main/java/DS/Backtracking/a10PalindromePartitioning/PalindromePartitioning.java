package DS.Backtracking.a10PalindromePartitioning;

import java.util.ArrayList;
import java.util.List;

/**
 * https://leetcode.com/problems/palindrome-partitioning/
 *
 * Pattern: Backtracking
 *
 * Difficulty: Medium
 *
 * Time Complexity: O(n × 2^n)
 * Space Complexity: O(n) (excluding output)
 */
public class PalindromePartitioning {

    public List<List<String>> partition(String s) {

        List<List<String>> result = new ArrayList<>();

        backtrack(0, s, new ArrayList<>(), result);

        return result;
    }

    private void backtrack(int start, String s, List<String> partial, List<List<String>> result) {

        if (start == s.length()) {
            result.add(new ArrayList<>(partial));
            return;
        }

        for (int end = start; end < s.length(); end++) {
            if (isPalindrome(s, start, end)) {
                partial.add(s.substring(start, end + 1));
                backtrack(end + 1, s, partial, result);
                partial.remove(partial.size() - 1);
            }
        }
    }

    private boolean isPalindrome(String s, int left, int right) {

        while (left < right) {

            if (s.charAt(left) != s.charAt(right))
                return false;

            left++;
            right--;
        }

        return true;
    }
}