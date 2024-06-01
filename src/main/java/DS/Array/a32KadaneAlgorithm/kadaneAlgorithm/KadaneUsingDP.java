package DS.Array.a32KadaneAlgorithm.kadaneAlgorithm;

public class KadaneUsingDP {

    // Function to find the largest contiguous array sum
    public static void maxSubArraySum(int[] a) {
        int size = a.length;
        int[] dp = new int[size];
        dp[0] = a[0];
        int ans = dp[0];
        for (int i = 1; i < size; i++) {
            dp[i] = Math.max(a[i], a[i] + dp[i - 1]);
            ans = Math.max(ans, dp[i]);
        }
        System.out.println(ans);
    }

    public static void main(String[] args) {
        int[] a = {-2, -3, 4, -1, -2, 1, 5, -3};
        maxSubArraySum(a); // Call the function to find and print the maximum contiguous array sum
    }
}