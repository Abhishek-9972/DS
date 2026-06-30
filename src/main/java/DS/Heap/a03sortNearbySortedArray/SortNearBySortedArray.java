package DS.Heap.a03sortNearbySortedArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.PriorityQueue;

public class SortNearBySortedArray {
    public static void main(String[] args) {
        int[] arr = {6,5,3,2,8,10,9};
        int k = 3;

        SortNearBySortedArray.kSort(arr, arr.length, k);
    }

    private static void kSort(int[] arr, int n, int k)
    {

        // min heap
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();

        // add first k + 1 items to the min heap
        for(int i = 0; i < k + 1; i++)
        {
            priorityQueue.add(arr[i]);
        }

        int index = 0;
        for(int i = k + 1; i < n; i++)
        {
            arr[index++] = priorityQueue.poll();
            priorityQueue.add(arr[i]);
        }

        Iterator<Integer> itr = priorityQueue.iterator();

        while(itr.hasNext())
        {
            arr[index++] = priorityQueue.peek();
            priorityQueue.poll();
        }

        System.out.println(Arrays.toString(arr));

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