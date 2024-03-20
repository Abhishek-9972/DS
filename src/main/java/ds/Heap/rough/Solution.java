package ds.Heap.rough;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Solution {
    public List<List<Integer>> kSmallestPairs(int[] nums1, int[] nums2, int k) {

        List<List<Integer>> list = new ArrayList();

        if(k==0)
        {
            return list;
        }

        if(nums1.length == 0 || nums2.length == 0)
        {
            return list;
        }

        PriorityQueue<Pair> pq = new PriorityQueue<>(
                new Comparator<Pair>(){
                    public int compare(Pair p1, Pair p2){
                        if(p1.getKey() == p2.getKey())
                        {
                            return p1.getValue() - p2.getValue();
                        }
                        else{
                            return p1.getKey() - p2.getKey();
                        }
                    }
                });


        for(int i =0; i<nums1.length; i++)
        {
            for(int j = 0; j<nums2.length; j++)
            {
                pq.add(new Pair(nums1[i], nums2[j]));
            }
        }



        for(int j = 0; j<k; j++){
            List<Integer> lt = new ArrayList();
            Pair pair = pq.remove();
            lt.add(pair.getKey());
            lt.add(pair.getValue());
            list.add(lt);
        }

        return list;
    }

    class Pair{
        int key;
        int value;

        Pair(int key, int value){
            this.key = key;
            this.value= value;
        }

        public void setKey(int key){
            this.key = key;
        }

        public void setValue(int value){
            this.value = value;
        }

        public int getKey(){
            return key;
        }

        public int getValue(){
            return value;
        }
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        List<List<Integer>> lists = solution.kSmallestPairs(new int[]{1,2,4,5,6}, new int[]{3,5,7,9}, 3);
        System.out.println(lists);
    }
}
