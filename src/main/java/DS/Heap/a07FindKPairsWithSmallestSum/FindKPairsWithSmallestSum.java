package DS.Heap.a07FindKPairsWithSmallestSum;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class FindKPairsWithSmallestSum {

    public List<List<Integer>> kSmallestPairs(int[] nums1, int[] nums2, int k) {

        List<List<Integer>> result = new ArrayList<>();

        if (nums1.length == 0 || nums2.length == 0 || k == 0) {
            return result;
        }

        PriorityQueue<Node> minHeap =
                new PriorityQueue<>((a, b) -> a.sum - b.sum);

        // Insert first element of each row
        // Suppose nums1 length = 100000 k = 3 -> Do we need to insert - 100000 rows? -> Hence Math.min(nums1.length, k)
        for (int i = 0; i < Math.min(nums1.length, k); i++) {
            minHeap.offer(new Node(i, 0, nums1[i] + nums2[0]));
        }

        // Either heap becomes empty or answer contains k pairs.
        while (!minHeap.isEmpty() && result.size() < k) {

            Node current = minHeap.poll();

            List<Integer> pair = new ArrayList<>();
            pair.add(nums1[current.i]);
            pair.add(nums2[current.j]);

            result.add(pair);

            // Push next element from same row
            if (current.j + 1 < nums2.length) {

                minHeap.offer(
                        new Node(
                                current.i,
                                current.j + 1,
                                nums1[current.i] + nums2[current.j + 1]
                        )
                );
            }
        }

        return result;
    }

    class Node {

        int i;
        int j;
        int sum;

        Node(int i, int j, int sum) {
            this.i = i;
            this.j = j;
            this.sum = sum;
        }
    }
}