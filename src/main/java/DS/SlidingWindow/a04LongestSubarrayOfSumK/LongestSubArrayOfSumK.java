package DS.SlidingWindow.a04LongestSubarrayOfSumK;

class LongestSubArrayOfSumK {

    static int lenOfLongSubarr(int A[], int N, int K) {

        int i = 0, j = 0, sum = 0;
        int maxLen = Integer.MIN_VALUE;

        while (j < N) {
            sum = sum + A[j];
            if (sum < K) {
                j++;
            } else if (sum == K) {
                maxLen = Math.max(maxLen, j - i + 1);
                j++;
            } else if (sum > K) {
                while (sum > K) {
                    sum -= A[i];
                    i++;
                }
                if (sum == K) {
                    maxLen = Math.max(maxLen, j - i + 1);
                }
                j++;
            }
        }
        return maxLen;
    }


    // Driver code
    public static void main(String args[]) {
        int arr[] = {10, 5, 2, 7, 1, 9};
        int n = arr.length;
        int k = 15;
        System.out.printf("Length = %d", lenOfLongSubarr(arr, n, k));
    }
}