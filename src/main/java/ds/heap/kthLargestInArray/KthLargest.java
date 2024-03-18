package ds.heap.kthLargestInArray;

import java.util.PriorityQueue;

public class KthLargest {

    public static void main(String[] args) {
        int [] arr = {7, 1, 3, 5 , 2, 10};
        KthLargest kthLargest = new KthLargest();
        System.out.println(kthLargest.findKthLargest(arr, 3));
    }

    public int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int i : nums) {
            minHeap.add(i);
            if (minHeap.size() > k) {
                minHeap.remove();
            }
        }
        return minHeap.remove();
    }
}
