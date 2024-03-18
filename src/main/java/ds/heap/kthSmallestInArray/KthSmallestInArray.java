package ds.heap.kthSmallestInArray;

import java.util.Collections;
import java.util.PriorityQueue;

public class KthSmallestInArray {
    public static void main(String[] args) {
        int[] arr = {7, 10, 4, 3, 20, 15};
        KthSmallestInArray kthSmallestInArray = new KthSmallestInArray();
        System.out.println(kthSmallestInArray.findKthSmallest(arr, 3));
    }

    public int findKthSmallest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(Collections.reverseOrder());
        for (int i : nums) {
            minHeap.add(i);
            if (minHeap.size() > k) {
                minHeap.remove();
            }
        }
        return minHeap.remove();
    }
/*
20
15
10
7
4
3
 */
    public int findKthSmallest1(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(Collections.reverseOrder());
        for (int i : nums) {
            minHeap.add(i);
        }

        for(int i : nums)
        {
            System.out.println(minHeap.remove());
        }


        return 1;
    }


    /*
3
4
7
10
15
20
 */
    public int findKthSmallest11(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int i : nums) {
            minHeap.add(i);
        }

        for(int i : nums)
        {
            System.out.println(minHeap.remove());
        }


        return 1;
    }
}