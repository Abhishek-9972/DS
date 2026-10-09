package DS.DP.LCS.a06minimumInsertionAndDeletion;

public class MinimumInsertionAndDeletion {

    public static void main(String[] args) {

        String a = "heap";
        String b = "pea";

        MinimumInsertionAndDeletion solution =
                new MinimumInsertionAndDeletion();

        int lcs = solution.LCSTabulation(
                a, b, a.length(), b.length());

        int minNoOfDeletion = a.length() - lcs;
        int minNoOfInsertion = b.length() - lcs;

        System.out.println("Minimum deletions: " + minNoOfDeletion);
        System.out.println("Minimum insertions: " + minNoOfInsertion);
    }

    private int LCSTabulation(String x, String y, int m, int n) {

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {

                if (x.charAt(i - 1) == y.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(
                            dp[i - 1][j],
                            dp[i][j - 1]);
                }
            }
        }

        return dp[m][n];
    }
}