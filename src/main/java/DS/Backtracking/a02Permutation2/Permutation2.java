package DS.Backtracking.a02Permutation2;

import java.util.ArrayList;
import java.util.List;

/**
 * https://leetcode.com/problems/permutations-ii/description/
 */
public class Permutation2 {
        public List<List<Integer>> permuteUnique(int[] nums) {
            List<List<Integer>> list = new ArrayList<>();
            return permutation(nums, new ArrayList<>(), new boolean[nums.length],list);
        }

        public List<List<Integer>> permutation(int[] input, List<Integer> partial, boolean[] used, List<List<Integer>> list){
            if(input.length==partial.size()){
                if(!list.contains(partial)){
                    list.add(new ArrayList<>(partial));
                }
                return list;
            }

            for(int i =0; i<input.length; i++){
                if(!used[i]){
                    used[i] = true;
                    partial.add(input[i]);
                    permutation(input, partial, used,list);
                    partial.remove(partial.size()-1);
                    used[i] = false;
                }
            }
            return list;
        }
    }

