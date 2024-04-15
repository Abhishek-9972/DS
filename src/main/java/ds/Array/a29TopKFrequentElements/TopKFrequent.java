package ds.Array.a29TopKFrequentElements;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class TopKFrequent {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer,Integer> map = new HashMap<>();
        for(int i : nums){
            if(map.containsKey(i)){
                map.put(i, map.get(i)+1);
            }
            else{
                map.put(i,1);
            }
        }

        PriorityQueue<Map.Entry<Integer,Integer>> pq = new PriorityQueue<>(
                new Comparator<Map.Entry<Integer,Integer>>(){
                    public int compare(Map.Entry<Integer,Integer> m1, Map.Entry<Integer,Integer> m2){
                        return m1.getValue()- m2.getValue();
                    }
                }
        );

        for(Map.Entry<Integer,Integer> m :map.entrySet()){
            pq.add(m);

            if(pq.size()>k){
                pq.remove();
            }
        }

        int[] res = new int[k];
        for(int i=0; i<k; i++){
            Map.Entry<Integer,Integer> m1 = pq.remove();
            res[i] = m1.getKey();
        }

        return res;
    }
}