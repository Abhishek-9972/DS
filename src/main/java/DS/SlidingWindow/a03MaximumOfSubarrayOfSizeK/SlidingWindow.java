package DS.SlidingWindow.a03MaximumOfSubarrayOfSizeK;

import java.util.Deque;
import java.util.LinkedList;

public class SlidingWindow {

    static void printMax(int arr[], int N, int K)
    {

        Deque<Integer> Qi = new LinkedList<Integer>();

        int i;
        for (i = 0; i < K; ++i) {

            while (!Qi.isEmpty()
                    && arr[i] >= arr[Qi.peekLast()])

                Qi.removeLast();

            Qi.addLast(i);
        }

        for (; i < N; ++i) {

            System.out.print(arr[Qi.peek()] + " ");

            while ((!Qi.isEmpty()) && Qi.peek() <= i - K)
                Qi.removeFirst();

            while ((!Qi.isEmpty())
                    && arr[i] >= arr[Qi.peekLast()])
                Qi.removeLast();

            Qi.addLast(i);
        }

        System.out.print(arr[Qi.peek()]);
    }

    // Driver's code
    public static void main(String[] args)
    {
        int arr[] = { 12, 1, 78, 90, 57, 89, 56 };
        int K = 3;

        // Function call
        printMax(arr, arr.length, K);
    }
}

/**
 * https://www.geeksforgeeks.org/sliding-window-maximum-maximum-of-all-subarrays-of-size-k/
 */