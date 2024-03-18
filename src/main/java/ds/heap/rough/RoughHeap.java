package ds.heap.rough;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class RoughHeap {
    public static void main(String[] args) {
       RoughHeap.example4();
    }

    public static void example1(){
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();
        for(int i = 0; i< 5; i++)
        {
            priorityQueue.add(i);
        }

        for(int i = 0; i<5; i++)
        {
            System.out.println(priorityQueue.remove());
        }
    }

    public static void example2(){
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }
        );
        for(int i = 0; i< 5; i++)
        {
            priorityQueue.add(i);
        }

        for(int i = 0; i<5; i++)
        {
            System.out.println(priorityQueue.remove());
        }
    }

    public static void example3(){

        int[] arr = {5, 6, 7, 8, 9};
        int k = 3;
        int x = 7;

        PriorityQueue<Pair> priorityQueue = new PriorityQueue<>(
                new Comparator<Pair>() {
                    @Override
                    public int compare(Pair p1, Pair p2) {
                        return p1.getValue() - p2.getValue();
                    }
                }
        );

        for(int i = 0; i< arr.length; i++)
        {
            priorityQueue.add(new Pair(arr[i], Math.abs(arr[i]-x)));
        }

        for(int i = 0; i<5; i++)
        {
            System.out.println(priorityQueue.remove().getKey());
        }
    }

    public static void example4(){

        int [] arr = {1,1,2,3,5,3,3,3,3,3,2,2,1,1};
        int n = arr.length;
        int k = 2;

        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            if (map.containsKey(arr[i])) {
                map.put(arr[i], map.get(arr[i]) + 1);
            } else {
                map.put(arr[i], 1);
            }
        }

        PriorityQueue<Map.Entry<Integer,Integer>> priorityQueue = new PriorityQueue<>(
                new Comparator<Map.Entry<Integer,Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer,Integer> m1, Map.Entry<Integer,Integer> m2) {
                        return m2.getValue() - m1.getValue();
                    }
                }
        );

        for(Map.Entry<Integer,Integer> m1 : map.entrySet())
        {
            priorityQueue.add(m1);
        }

        for(int i = 0; i<map.size(); i++)
        {
            System.out.println(priorityQueue.remove().getKey());
        }
    }
}
