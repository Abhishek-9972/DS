package DS.String.a10PalindromicSubstrings;

/**
 * https://leetcode.com/problems/palindromic-substrings/
 */
public class PalindromicSubstrings {

    int count = 0;

    public int countSubstrings(String s) {

        for (int i = 0; i < s.length(); i++) {

            // Odd length palindrome
            expandAroundCenter(s, i, i);

            // Even length palindrome
            expandAroundCenter(s, i, i + 1);
        }

        return count;
    }

    private void expandAroundCenter(String s, int left, int right) {

        while (left >= 0 &&
                right < s.length() &&
                s.charAt(left) == s.charAt(right)) {

            count++;

            left--;
            right++;
        }
    }
}