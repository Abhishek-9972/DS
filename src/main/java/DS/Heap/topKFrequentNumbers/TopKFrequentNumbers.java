package DS.Heap.topKFrequentNumbers;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class TopKFrequentNumbers {

    public static void main(String[] args) {
        int [] arr = {1,1,2,2,3,5,5,5,5,5,3,2,1};
        int n = arr.length;
        int k = 3;
        TopKFrequentNumbers.printTopKFrequentNumbers(arr, n, k);
    }
    public static void printTopKFrequentNumbers(int[] arr, int n, int k) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            if (map.containsKey(arr[i])) {
                map.put(arr[i], map.get(arr[i]) + 1);
            } else {
                map.put(arr[i], 1);
            }
        }

        /*PriorityQueue<Map.Entry<Integer, Integer> > queue =
                new PriorityQueue<>(
                        (a, b) -> a.getValue().equals(b.getValue()) ?
                                Integer.compare(b.getKey(), a.getKey()) :
                                Integer.compare(b.getValue(), a.getValue()));*/

        PriorityQueue<Map.Entry<Integer, Integer> > queue =
                new PriorityQueue<>(new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                });

        for(Map.Entry<Integer, Integer> entry: map.entrySet())
        {
            queue.add(entry);
            if(queue.size()>k)
            {
                queue.remove();
            }
        }

        for(int i=0; i<k; i++)
        {
            System.out.println(queue.poll().getKey());
        }
    }
}
