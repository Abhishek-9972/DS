package ds.DP.LCS.shortestCommonSupersequence;

public class ShortestCommonSuperSequence {

    public static void main(String[] args) {
        String a = "AGGTAB";
        String b = "GXTXAYB";
        ShortestCommonSuperSequence shortestCommonSuperSequence = new ShortestCommonSuperSequence();
        int lcs = shortestCommonSuperSequence.LCSTabulation(a, b, a.length(), b.length());
        System.out.println((a.length()+b.length())-lcs);
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
