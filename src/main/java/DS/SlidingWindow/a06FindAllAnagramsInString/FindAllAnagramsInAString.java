package DS.SlidingWindow.a06FindAllAnagramsInString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindAllAnagramsInAString {

    List<Integer> findAnagrams(String s, String p) {

        int[] pCount = new int[26];
        int[] sCount = new int[26];
        List<Integer> result = new ArrayList<>();

        // Count frequency of characters in p
        for (char c : p.toCharArray()) {
            pCount[c - 'a']++;
        }

        // Sliding window
        for (int left = 0; left < s.length(); left++) {

            // Add current character to the window
            sCount[s.charAt(left) - 'a']++;

            // Remove the character that is out of the window
            if (left >= p.length()) {
                sCount[s.charAt(left - p.length()) - 'a']--;
            }

            // Compare frequencies
            if (Arrays.equals(pCount, sCount)) {
                // Start index of the anagram
                result.add(left - p.length() + 1);
            }
        }

        return result;
    }
}