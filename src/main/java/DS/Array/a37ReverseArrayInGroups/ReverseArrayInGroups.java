package DS.Array.a37ReverseArrayInGroups;

public class ReverseArrayInGroups {

    public static void reverse(int[] arr, int n, int k) {
        int j = 0, i = k;

        while (i < n) {
            reverseArray(arr, j, i - 1);

            i += k;
            j += k;
        }

        reverseArray(arr, j, n - 1);
    }

    private static void reverseArray(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
    }

    // Driver code
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8};
        int k = 3;
        int n = arr.length;

        reverse(arr, n, k);

        for (int i = 0; i < n; i++)
            System.out.print(arr[i] + " ");
    }
}

