package DS.String.a05LongestSubstringWithoutRepeatingNumbers;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class LongestSubstringWithoutRepeatingCharacters {

    int lengthOfLongestSubstring(String s) {

        Set<Character> charSet = new HashSet<>();

        int maxLength = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {

            while (charSet.contains(s.charAt(right))) {
                charSet.remove(s.charAt(left));
                left++;
            }

            charSet.add(s.charAt(right));
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }

}
