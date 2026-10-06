package DS.Heap.a01kthLargestInArray;

import java.util.PriorityQueue;

/**
 * https://leetcode.com/problems/kth-largest-element-in-an-array/description/
 * Input: nums = [3,2,1,5,6,4], k = 2
 * Output: 5
 */
public class KthLargest {

    public static void main(String[] args) {
        int [] arr = {7, 1, 3, 5 , 2, 10};
        KthLargest kthLargest = new KthLargest();
        System.out.println(kthLargest.findKthLargest(arr, 3));
    }

    public int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int num : nums) {
            minHeap.offer(num);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }
        return minHeap.peek();
    }
}
