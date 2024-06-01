package DS.DP.LCS.LongestPalindromicSubSequence;

import java.util.Arrays;

public class LongestPalindromicSubsequence {
    public static void main(String[] args) {
        String x = "agbcba";
        String y = reverseString(x);
        LongestPalindromicSubsequence longestPalindromicSubsequence = new LongestPalindromicSubsequence();
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i < dp.length; i++) {
            Arrays.fill(dp[i], -1);
        }
        System.out.println(longestPalindromicSubsequence.LCSTabulation(x, y, x.length(), y.length()));

    }

    public static String reverseString(String s){
//Converting the string into a character array
        char c[]=s.toCharArray();
        String reverse="";
//For loop to reverse a string
        for(int i=c.length-1;i>=0;i--){
            reverse+=c[i];
        }
        return reverse;
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
