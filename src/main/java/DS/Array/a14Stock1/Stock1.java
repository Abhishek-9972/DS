package DS.Array.a14Stock1;

/**
 * https://leetcode.com/problems/best-time-to-buy-and-sell-stock/description/
 */
public class Stock1 {
    public int maxProfit(int[] prices) {
        int min = Integer.MAX_VALUE;
        int max = 0;

        for (int i = 0; i < prices.length; i++) {
            if (prices[i] < min) {
                min = prices[i];
            } else {
                max = Math.max(max, prices[i] - min);
            }
        }
        return max;
    }
}

