package DS.SlidingWindow.a03MaximumOfSubarrayOfSizeK;

import java.util.Deque;
import java.util.LinkedList;

/**
 * https://leetcode.com/problems/sliding-window-maximum/
 */
public class SlidingWindow {

    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;

        //Step1 : Initialize the deque and result array
        //Deque stores indices and not values
        Deque<Integer> deque = new LinkedList<>();
        int[] result = new int[n - k + 1];

        //Step2 : SetUp deque for the first k elements
        for (int i = 0; i < k; i++) {
            //Remove all smaller elements from the back
            while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[i]) {
                deque.pollLast();
            }
            deque.offerLast(i);
        }

        //The front of the deque is the max of the first window
        result[0] = nums[deque.peekFirst()];

        //Process the remaining elements
        for (int i = k; i < n; i++) {
            //Remove the element that has slid out of the window
            if (deque.peekFirst() <= i - k) {
                deque.pollFirst();
            }

            //Remove all the elements smaller than the incoming element
            while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[i]) {
                deque.pollLast();
            }

            //Add current element's index
            deque.offerLast(i);

            //The front of the deque is always the max of the window
            result[i - k + 1] = nums[deque.peekFirst()];
        }
        return result;

    }

    // Driver's code
    public static void main(String[] args) {
        int arr[] = {12, 1, 78, 90, 57, 89, 56};
        int K = 3;

        // Function call
        SlidingWindow slidingWindow = new SlidingWindow();
        int[] result = slidingWindow.maxSlidingWindow(arr, K);
        for (int ele: result){
            System.out.print(ele + " ");
        }

    }
}

/**
 * https://www.geeksforgeeks.org/sliding-window-maximum-maximum-of-all-subarrays-of-size-k/
 */