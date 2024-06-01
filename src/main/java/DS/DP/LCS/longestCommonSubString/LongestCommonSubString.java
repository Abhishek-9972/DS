package DS.DP.LCS.longestCommonSubString;

public class LongestCommonSubString {

    int max = 0;

    public static void main(String[] args) {
        String x = "abcdgh";
        String y = "abedfhr";
        LongestCommonSubString longestCommonSubString = new LongestCommonSubString();
        System.out.println(longestCommonSubString.LCSTabulation(x, y, x.length(), y.length()));
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
                    max = Math.max(dp[i][j], max);
                } else {
                    dp[i][j] = 0;
                }
            }
        }
        return max;
    }
}
