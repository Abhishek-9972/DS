package DS.Array.Hashing.a11GroupAnagrams;

import java.util.*;

/**
 * https://leetcode.com/problems/group-anagrams/description/
 */
public class GroupAnagrams {

    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> map = new HashMap<>();
        for (String s : strs) {
            // Sorted string acts as the key for all anagrams
            char[] chars = s.toCharArray();
            Arrays.sort(chars);

            String key = new String(chars);

            // Add the string to the group
            if (map.containsKey(key)) {
                map.get(key).add(s);
            } else {
                List<String> list = new ArrayList<>();
                list.add(s);
                map.put(key, list);
            }
        }
        return new ArrayList<>(map.values());
    }
}