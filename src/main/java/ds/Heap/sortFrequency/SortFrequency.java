package ds.Heap.sortFrequency;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class SortFrequency {
    public static void main(String[] args) {
        int [] arr = {1,1,2,3,5,3,3,3,3,3,2,2,1,1};
        int n = arr.length;
        int k = 2;
        SortFrequency.printTopKFrequentNumbers(arr, n);
    }
    public static void printTopKFrequentNumbers(int[] arr, int n) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            if (map.containsKey(arr[i])) {
                map.put(arr[i], map.get(arr[i]) + 1);
            } else {
                map.put(arr[i], 1);
            }
        }


        PriorityQueue<Map.Entry<Integer, Integer> > queue =
                new PriorityQueue<>(new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

        for(Map.Entry<Integer, Integer> entry: map.entrySet())
        {
            queue.add(entry);
        }

        for(int i=0; i<map.size(); i++)
        {
            System.out.println(queue.poll().getKey());
        }
    }
}
