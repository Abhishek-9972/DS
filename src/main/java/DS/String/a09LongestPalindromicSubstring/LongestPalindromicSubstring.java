package DS.String.a09LongestPalindromicSubstring;

/**
 * https://leetcode.com/problems/longest-palindromic-substring/
 * Time Complexity - O(n^2)
 */
public class LongestPalindromicSubstring {

    int start = 0;
    int maxLength = 0;

    public String longestPalindrome(String s) {

        if (s == null || s.length() < 2) {
            return s;
        }

        for (int i = 0; i < s.length(); i++) {

            // Odd length palindrome
            expandAroundCenter(s, i, i);

            // Even length palindrome
            expandAroundCenter(s, i, i + 1);
        }

        return s.substring(start, start + maxLength);
    }

    private void expandAroundCenter(String s, int left, int right) {

        while (left >= 0 &&
                right < s.length() &&
                s.charAt(left) == s.charAt(right)) {

            left--;
            right++;
        }

        int length = right - left - 1; // After the loop stops, left and right are just outside the palindrome.

        if (length > maxLength) {
            maxLength = length;
            start = left + 1; // Because left has moved one position outside the palindrome. We need the first valid index, which is left + 1.
        }
    }
}