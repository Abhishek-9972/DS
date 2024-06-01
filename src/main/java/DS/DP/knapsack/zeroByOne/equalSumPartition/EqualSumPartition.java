package DS.DP.knapsack.zeroByOne.equalSumPartition;


public class EqualSumPartition {
    public static void main(String[] args) {
        int set[] = {1, 5, 11, 5};
        int n = set.length;
        EqualSumPartition equalSumPartition = new EqualSumPartition();
        boolean equalSumPartitionCheck = equalSumPartition.equalPartition(set, n);
        System.out.println(equalSumPartitionCheck);
    }

    private boolean equalPartition(int [] arr, int n){
        EqualSumPartition equalSumPartition = new EqualSumPartition();
        int sum = 0;
        for(int i = 0; i<arr.length; i++)
        {
            sum = sum + arr[i];
        }
        if(sum % 2 !=0)
        {
            return false;
        }

        return equalSumPartition.findSubset(arr, n, sum/2);
    }

    private boolean findSubset(int[] arr, int n, int sum) {
        boolean dp[][] = new boolean[n + 1][sum + 1];

        // If sum is 0, then answer is true
        for (int i = 1; i <= sum; i++)
            dp[0][i] = false;

        // If sum is not 0 and set is empty,
        // then answer is false
        for (int i = 0; i <= n; i++)
            dp[i][0] = true;

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < sum + 1; j++) {
                if (arr[i - 1] <= j) {
                    dp[i][j] = dp[i - 1][j - arr[i - 1]] || dp[i - 1][j];
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][sum];
    }
}
