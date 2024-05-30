package ds.SlidingWindow.a06FindAllAnagramsInString;

import java.util.ArrayList;
import java.util.List;

public class FindAllAnagramsInString {
    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> result = new ArrayList<>();

        if (s.length() == 0 || s == null) {
            return result;
        }

        int[] charMap = new int[26];
        for (char c : p.toCharArray()) {
            charMap[c - 'a']++;
        }

        int left = 0;
        int right = 0;
        int count = p.length();

        while (right < s.length()) {
            if (charMap[s.charAt(right++) - 'a']-- >= 1) {
                count--;
            }

            if (count == 0) {
                result.add(left);
            }

            if (right - left == p.length() && charMap[s.charAt(left++) - 'a']++ >= 0) {
                count++;
            }
        }
        return result;

    }
}