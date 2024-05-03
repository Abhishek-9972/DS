package ds.String.a02ValidAnagram;

import java.util.HashMap;
import java.util.Map;

public class ValidAnagram {
        public boolean isAnagram(String s, String t) {
            Map<Character, Integer> map = new HashMap<>();

            for(char ch : s.toCharArray()){
                if(map.containsKey(ch)){
                    map.put(ch, map.get(ch) + 1);
                } else {
                    map.put(ch, 1);
                }
            }

            for(char ch : t.toCharArray()){
                if(map.containsKey(ch)){
                    map.put(ch, map.get(ch) - 1);
                    if(map.get(ch)<0){return false;}
                } else {
                    return false;
                }
            }

            for(Map.Entry<Character, Integer> m1: map.entrySet()){
                if(m1.getValue()!=0){
                    return false;
                }
            }

            return true;
        }
    }

