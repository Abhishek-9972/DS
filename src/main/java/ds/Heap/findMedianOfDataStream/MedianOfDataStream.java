package ds.Heap.findMedianOfDataStream;

import java.util.Collections;
import java.util.PriorityQueue;

public class MedianOfDataStream {
    private final PriorityQueue<Integer> minHeap;
    private final PriorityQueue<Integer> maxHeap;
    private boolean even;

    public MedianOfDataStream() {
        this.even = true;
        this.minHeap = new PriorityQueue<Integer>();
        this.maxHeap = new PriorityQueue<Integer>((a, b) -> Integer.compare(b, a));
    }

    public void addNum(final int num) {
        if (even) {
            this.minHeap.offer(num);
            this.maxHeap.offer(this.minHeap.poll());
        } else {
            this.maxHeap.offer(num);
            this.minHeap.offer(this.maxHeap.poll());
        }

        even = !even;
    }

    public double findMedian() {
        return even ? (this.minHeap.peek() + this.maxHeap.peek()) / 2.0 : this.maxHeap.peek();
    }
}


// [1, 2, 3, 5, 6]
// [3, 2, 1], [5, 6]
// poll from max heap to get median -> 3

// Odd
// [7, 3, 2, 1], [5, 6] <- add 7 to max
// [3, 2, 1], [5, 6, 7] <- poll from max and add to min
// poll from max and min -> iMax + iMin / 2

// Even
// [3, 2, 1], [5, 6, 7] <- add 4
// [3, 2, 1], [4, 5, 6, 7] <- add 4 to min
// [4, 3, 2, 1], [5, 6, 7] <- poll from min and add to max
// poll from max to get median

/**
 * Your MedianFinder object will be instantiated and called as such:
 * MedianFinder obj = new MedianFinder();
 * obj.addNum(num);
 * double param_2 = obj.findMedian();
 */