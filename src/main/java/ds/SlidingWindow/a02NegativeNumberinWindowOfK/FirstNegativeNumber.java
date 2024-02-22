package ds.SlidingWindow.a02NegativeNumberinWindowOfK;
import java.util.Deque;
import java.util.LinkedList;

public class FirstNegativeNumber {
    public static void printFirstNegative(int[] arr, int n, int k) {
        Deque<Integer> negIndices = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            if (!negIndices.isEmpty() && negIndices.peekFirst() == i - k) {
                negIndices.pollFirst();
            }

            if (arr[i] < 0) {
                negIndices.offerLast(i);
            }

            if (i >= k - 1) {
                if (!negIndices.isEmpty()) {
                    System.out.print(arr[negIndices.peekFirst()] + " ");
                } else {
                    System.out.print("0 ");
                }
            }
        }
    }

    // Driver code
    public static void main(String[] args) {
        int[] arr = { 12, -1, -7, 8, -15, 30, 16, 28 };
        int n = arr.length;
        int k = 3;
        printFirstNegative(arr, n, k); // Output: -1 -1 -7 -15 -15 0
    }
}

/**
 * https://www.geeksforgeeks.org/first-negative-integer-every-window-size-k/
 */