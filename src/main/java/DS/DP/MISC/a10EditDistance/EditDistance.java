package DS.DP.MISC.a10EditDistance;

public class EditDistance {

    public int minDistance(String word1, String word2) {

        int m = word1.length();
        int n = word2.length();

        int[][] dp = new int[m + 1][n + 1];

        // If word2 is empty, delete all characters from word1
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        // If word1 is empty, insert all characters of word2
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {

                    dp[i][j] = dp[i - 1][j - 1];

                } else {

                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j - 1],                 // Replace
                            Math.min(dp[i - 1][j],            // Delete
                                    dp[i][j - 1])            // Insert
                    );
                }
            }
        }

        return dp[m][n];
    }
}