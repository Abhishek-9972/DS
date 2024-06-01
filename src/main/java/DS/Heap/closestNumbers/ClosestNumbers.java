package DS.Heap.closestNumbers;

import DS.Heap.sortNearbySortedArray.Pair;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ClosestNumbers {
    public static void main(String[] args) {
        int[] arr = {5, 6, 7, 8, 9};
        int k = 3;
        int x = 7;

        PriorityQueue<Pair> pq = new PriorityQueue<>(new Comparator<Pair>() {
            @Override
            public int compare(Pair pair1, Pair pair2) {
                return pair2.getValue().compareTo(pair1.getValue());
            }
        });

        for (int i = 0; i < arr.length; i++) {
            pq.add(new Pair(arr[i], Math.abs(arr[i] - x)));
            if (pq.size() > k) {
                pq.poll();
            }
        }

        while (pq.size() > 0) {
            System.out.println(pq.poll().getKey());
        }
    }
}
