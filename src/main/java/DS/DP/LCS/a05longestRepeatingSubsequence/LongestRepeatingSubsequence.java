package DS.DP.LCS.a05longestRepeatingSubsequence;

public class LongestRepeatingSubsequence {
    public static void main(String[] args) {
        String x = "aabb";
        LongestRepeatingSubsequence longestRepeatingSubsequence = new LongestRepeatingSubsequence();
        int i = longestRepeatingSubsequence.longestIncreasingSubsequence(x);
        System.out.println(i);
    }

    private int longestIncreasingSubsequence(String x){
        LongestRepeatingSubsequence longestRepeatingSubsequence = new LongestRepeatingSubsequence();
        String revString = "bbaaa";
        return longestRepeatingSubsequence.LCSTabulation(x, revString, x.length(), revString.length());
    }

    private int LCSTabulation(String x, String y, int m, int n) {

        if (m == 0 || n == 0) {
            return 0;
        }

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (x.charAt(i - 1) == y.charAt(j - 1) && i!=j) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i][j - 1], dp[i - 1][j]);
                }
            }
        }
        return dp[m][n];
    }

}
