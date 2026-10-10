package DS.String.a04LongestPalindrome;

import java.util.HashMap;
import java.util.Map;

public class LongestPalindrome {
    public int longestPalindrome(String s) {
        Map<Character, Integer> map = new HashMap<>();

        for (char ch : s.toCharArray()) {
            map.put(ch, map.getOrDefault(ch, 0) + 1);
        }

        if (map.size() == 1) {
            return map.get(0);
        }

        int count = 0;
        int odd = 0;

        for (Map.Entry<Character, Integer> m1 : map.entrySet()) {
            if (m1.getValue() % 2 == 0) {
                count = count + m1.getValue();
            } else {
                odd = 1;
                count = count + m1.getValue() - 1;
            }
        }

        return count + odd;
    }
}

