package ds.Array.a33MissingNumberInArray;

public class MissingNumber {
    public static int getMissingNo(int[] nums, int n)
    {
        int sum = 0;
        int N = n+1;
        for(int i=0;i<n;i++){
            sum = sum + nums[i];
        }
        return ((N * (N+1))/2 - sum);
    }

    // Driver code
    public static void main(String[] args)
    {
        int[] arr = { 1, 2, 3, 5 };
        int N = arr.length;
        System.out.println(getMissingNo(arr, N));
    }
}
