package DS.DP.MISC.a13Stock3;

public class Stock3 {
    public int maxProfit(int[] prices) {
        int buy1 = Integer.MIN_VALUE;
        int sell1 = 0;
        int buy2 = Integer.MIN_VALUE;
        int sell2 = 0;

        for (int price : prices) {
            // First transaction: buy at the lowest effective price
            buy1 = Math.max(buy1, -price);

            // First transaction: sell and maximize profit
            sell1 = Math.max(sell1, buy1 + price);

            // Second transaction: buy using profit from first sale
            buy2 = Math.max(buy2, sell1 - price);

            // Second transaction: sell and maximize total profit
            sell2 = Math.max(sell2, buy2 + price);
        }
        return sell2;
    }
}