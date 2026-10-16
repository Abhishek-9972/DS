package DS.Heap.a05topKFrequentNumbers;

import java.util.*;

public class TopKFrequentNumbers {

    public static void main(String[] args) {
        int[] arr = {1, 1, 2, 2, 3, 5, 5, 5, 5, 5, 3, 2, 1};
        int n = arr.length;
        int k = 3;
        TopKFrequentNumbers topKFrequentNumbers = new TopKFrequentNumbers();
        topKFrequentNumbers.printTopKFrequentNumbers(arr, k);
    }

    public int[] printTopKFrequentNumbers(int[] nums, int k) {
        PriorityQueue<Map.Entry<Integer, Integer>> pq = new PriorityQueue<>((a,b) -> a.getValue().compareTo(b.getValue()));
        Map<Integer, Integer> map = new HashMap<>();
        for(int num: nums){
            map.put(num, map.getOrDefault(num, 0) + 1);
        }

        for(Map.Entry<Integer, Integer> entry: map.entrySet()){
            pq.add(entry);
            if(pq.size()>k){
                pq.remove();
            }
        }

        int[] result = new int[pq.size()];
        for(int i=0; i<result.length; i++){
            result[i] = pq.poll().getKey();
        }
        return result;
    }
}
