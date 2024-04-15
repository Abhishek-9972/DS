package ds.Array.a22GroupAnagrams;

import java.util.*;

/**
 * https://leetcode.com/problems/group-anagrams/description/
 */
public class GroupAnagrams {
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> map = new HashMap<>();
        for(String s : strs){
            char[] ch = s.toCharArray();
            Arrays.sort(ch);
            String sortedString = makeString(ch);
            if(map.containsKey(sortedString)){
                List<String> lt = map.get(sortedString);
                lt.add(s);
                map.put(sortedString, lt);
            } else{
                List<String> lt = new ArrayList<>();
                lt.add(s);
                map.put(sortedString, lt);
            }
        }

        List<List<String>> result = new ArrayList<>();
        for(Map.Entry<String, List<String>> m1 : map.entrySet()){
            result.add(m1.getValue());
        }
        return result;
    }

    public String makeString(char[] ch){
        String s = "";
        for(char c: ch){
            s = s + c;
        }
        return s;
    }
}