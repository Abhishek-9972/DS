package DS.DP.LCS.minimumInsertionAndDeletion;

public class MinimumInsertionAndDeletion {
    public static void main(String[] args) {
        String a = "heap";
        String b = "pea";
        MinimumInsertionAndDeletion minimumInsertionAndDeletion = new MinimumInsertionAndDeletion();
        int lcs = minimumInsertionAndDeletion.LCSTabulation(a, b, a.length(), b.length());
        int minNoOfDeletion = a.length()>b.length() ? a.length()-lcs : b.length() - lcs;
        int maxNoOfInsertion = a.length()<b.length() ? a.length()-lcs : b.length() - lcs;
        System.out.println(minNoOfDeletion );
        System.out.println(maxNoOfInsertion);


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
