package ds.Backtracking.a03Combination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.com/problems/combinations/description/
 */
public class Combination {

    class Solution {
        public List<List<Integer>> combine(int n, int k) {
            List<List<Integer>> list = new ArrayList<>();
            int [] arr = new int[n];
            for(int i =0; i<n; i++){
                arr[i] = i+1;
            }
            Arrays.sort(arr);
            backtrack(arr, new ArrayList<>(), 0, k, list);
            return list;
        }

        private void backtrack(int[] input, List<Integer> partial, int start, int k, List<List<Integer>> list){

            if(partial.size() == k) list.add(new ArrayList<>(partial));
            else{
                for(int i = start; i < input.length; i++){
                    partial.add(input[i]);
                    backtrack(input, partial, i+1, k, list); // not i + 1 because we can reuse same elements
                    partial.remove(partial.size() - 1);
                }
            }
        }
    }
}
