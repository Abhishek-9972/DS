package DS.String.a05LongestSubstringWithoutRepeatingNumbers;

import java.util.LinkedHashMap;
import java.util.Map;

public class LongestSubstringWithoutRepeatingCharacters {
    public int lengthOfLongestSubstring(String s) {
        Map<Character,Integer> map = new LinkedHashMap<>();
        int longestSubstringLength = 0;

        for(int i =0; i< s.length(); i++)
        {
            if(map.containsKey(s.charAt(i))){
                i = map.get(s.charAt(i));
                map.clear();
            }
            else{
                map.put(s.charAt(i), i);
            }

            if(map.size() > longestSubstringLength){
                longestSubstringLength = map.size();
            }
        }

        return longestSubstringLength;
    }
}
