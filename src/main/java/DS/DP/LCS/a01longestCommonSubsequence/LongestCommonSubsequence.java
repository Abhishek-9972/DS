package DS.DP.LCS.a01longestCommonSubsequence;

import java.util.Arrays;

public class LongestCommonSubsequence {
    public static void main(String[] args) {
        String x = "abcdgh";
        String y = "abedfhr";
        LongestCommonSubsequence longestCommonSubsequence = new LongestCommonSubsequence();
        System.out.println(longestCommonSubsequence.LCS(x, y, x.length(), y.length()));
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i < dp.length; i++) {
            Arrays.fill(dp[i], -1);
        }
        System.out.println(longestCommonSubsequence.LCSMemoization(x, y, x.length(), y.length(), dp));
        System.out.println(longestCommonSubsequence.LCSTabulation(x, y, x.length(), y.length()));

    }

    private int LCS(String x, String y, int m, int n) {
        if (m == 0 || n == 0) {
            return 0;
        }
        if (x.charAt(m - 1) == y.charAt(n - 1)) {
            return 1 + LCS(x, y, m - 1, n - 1);
        } else {
            return Math.max(LCS(x, y, m, n - 1), LCS(x, y, m - 1, n));
        }
    }

    private int LCSMemoization(String x, String y, int m, int n, int[][] dp) {
        if (m == 0 || n == 0) {
            return 0;
        }
        if (dp[m][n] != -1) {
            return dp[m][n];
        }
        if (x.charAt(m - 1) == y.charAt(n - 1)) {
            dp[m][n] = 1 + LCS(x, y, m - 1, n - 1);
            return dp[m][n];
        } else {
            dp[m][n] = Math.max(LCS(x, y, m, n - 1), LCS(x, y, m - 1, n));
            return dp[m][n];
        }
    }

    private int LCSTabulation(String x, String y, int m, int n) {

        if (m == 0 || n == 0) {
            return 0;
        }

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (x.charAt(i - 1) == y.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i][j - 1], dp[i - 1][j]);
                }
            }
        }
        return dp[m][n];
    }

}
