package DS.DP.MISC.a14Stock4;

import java.util.Arrays;

/**
 * LeetCode 188: Best Time to Buy and Sell Stock IV
 *
 * Pattern: Dynamic Programming
 * Difficulty: Hard
 * Time Complexity: O(n * k)
 * Space Complexity: O(k)
 */
public class Stock4 {

    public int maxProfit(int k, int[] prices) {
        int n = prices.length;

        if (n == 0 || k == 0) {
            return 0;
        }

        // If k is large enough, this becomes Stock 2.
        if (k >= n / 2) {
            int profit = 0;

            for (int i = 1; i < n; i++) {
                if (prices[i] > prices[i - 1]) {
                    profit += prices[i] - prices[i - 1];
                }
            }

            return profit;
        }

        // buy[t]  = best balance after buying for transaction t
        // sell[t] = best profit after completing transaction t
        int[] buy = new int[k + 1];
        int[] sell = new int[k + 1];

        Arrays.fill(buy, Integer.MIN_VALUE / 2);

        for (int price : prices) {
            // Descending order preserves previous transaction states.
            for (int t = k; t >= 1; t--) {
                sell[t] = Math.max(sell[t], buy[t] + price);
                buy[t] = Math.max(buy[t], sell[t - 1] - price);
            }
        }

        return sell[k];
    }
}