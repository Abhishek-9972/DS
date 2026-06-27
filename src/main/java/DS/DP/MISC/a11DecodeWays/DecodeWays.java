package DS.DP.MISC.a11DecodeWays;

/**
 * Time Complexity
 * O(n)
 * Space Complexity
 * O(n)
 */

public class DecodeWays {

    public int numDecodings(String s) {

        if (s == null || s.length() == 0 || s.charAt(0) == '0') {
            return 0;
        }

        int n = s.length();
        int[] dp = new int[n + 1];

        dp[0] = 1;
        dp[1] = 1;

        for (int i = 2; i <= n; i++) {

            // Single digit
            int oneDigit = Integer.parseInt(s.substring(i - 1, i));
            if (oneDigit >= 1 && oneDigit <= 9) {
                dp[i] = dp[i] + dp[i - 1];
            }

            // Two digits
            int twoDigits = Integer.parseInt(s.substring(i - 2, i));
            if (twoDigits >= 10 && twoDigits <= 26) {
                dp[i] = dp[i] + dp[i - 2];
            }
        }

        return dp[n];
    }
}
