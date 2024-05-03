package ds.String.a04LongestPalindrome;

import java.util.HashMap;
import java.util.Map;

public class LongestPalindrome {
        public int longestPalindrome(String s) {
            Map<Character, Integer> map = new HashMap<>();

            for(char ch: s.toCharArray()){
                if(map.containsKey(ch)){
                    map.put(ch, map.get(ch)+1);
                }else{
                    map.put(ch, 1);
                }
            }

            if(map.size()==1){
                for(Map.Entry<Character, Integer> m2 : map.entrySet()){
                    return m2.getValue();
                }
            }

            int count = 0;
            int odd = 0;

            for(Map.Entry<Character, Integer> m1 : map.entrySet()){
                if(m1.getValue()%2==0){
                    count = count + m1.getValue();
                }
                else{
                    odd = 1;
                    count = count + m1.getValue()-1;
                }
            }

            return count + odd;
        }
    }

