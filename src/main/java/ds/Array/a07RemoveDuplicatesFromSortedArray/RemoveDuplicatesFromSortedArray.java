package ds.Array.a07RemoveDuplicatesFromSortedArray;

import java.util.LinkedHashMap;
import java.util.Map;

public class RemoveDuplicatesFromSortedArray {
        public int removeDuplicates(int[] nums) {

            Map<Integer, Integer> map = new LinkedHashMap<>();
            for(int num : nums){
                if(map.containsKey(num)){
                    map.put(num, map.get(num)+1);
                }else{
                    map.put(num,1);
                }
            }

            int counter = 0;
            for(Map.Entry<Integer,Integer> m1: map.entrySet()){
                nums[counter]= m1.getKey();
                counter++;
            }

            return map.keySet().size();
        }
    }


