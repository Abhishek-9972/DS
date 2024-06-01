package DS.DP.knapsack.zeroByOne.zeroByOne;

import static java.lang.Math.max;

public class KnapsackZeroByOne {

    public static void main(String[] args) {
        int[] wt = {1, 3, 4, 5};
        int[] v = {1, 4, 5, 7};
        int w = 7;
        KnapsackZeroByOne knapsackZeroByOne = new KnapsackZeroByOne();
        int knapsack = knapsackZeroByOne.knapsack(wt, v, w, wt.length);
        System.out.println("0/1 Knapsack : " + knapsack);
    }

    private int knapsack(int[] wt, int[] v, int w, int n) {
        if (n == 0 || w == 0) {
            return 0;
        }
        if (wt[n - 1] <= w) {
            return max(v[n - 1] + knapsack(wt, v, w - wt[n - 1], n - 1), knapsack(wt, v, w, n - 1));
        } else {
            return knapsack(wt, v, w, n - 1);
        }
    }
}
