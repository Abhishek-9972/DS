package DS.Heap.a03sortNearbySortedArray;

import java.util.Arrays;
import java.util.PriorityQueue;

public class SortNearBySortedArray {

    public static void main(String[] args) {
        int[] arr = {6, 5, 3, 2, 8, 10, 9};
        int k = 3;
        kSort(arr, arr.length, k);
        System.out.println(Arrays.toString(arr));
    }

    private static void kSort(int[] arr, int n, int k) {

        // Min heap
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();

        // Add first k + 1 elements to the min heap
        for (int i = 0; i < Math.min(k + 1, n); i++) {
            priorityQueue.add(arr[i]);
        }

        int index = 0;
        // Remove smallest element and add next element
        for (int i = k + 1; i < n; i++) {
            arr[index++] = priorityQueue.poll();
            priorityQueue.add(arr[i]);
        }

        // Empty remaining heap in sorted order
        while (!priorityQueue.isEmpty()) {
            arr[index++] = priorityQueue.poll();
        }
    }
}

/**
 * Question
 *
 * Given an array where every element is at most k positions away from its correct sorted position, sort the array.
 *
 * Example 1
 *
 * Input
 *
 * nums = [6, 5, 3, 2, 8, 10, 9]
 * k = 3
 *
 * Output
 *
 * [2, 3, 5, 6, 8, 9, 10]
 */