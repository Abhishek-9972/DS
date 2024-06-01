package DS.String.a03RansomNote;

import java.util.HashMap;
import java.util.Map;

public class RansomNote {
        public boolean canConstruct(String ransomNote, String magazine) {
            char[] ch = magazine.toCharArray();
            Map<Character, Integer> map = new HashMap<>();
            for(char c : ch){
                if(map.containsKey(c)){
                    map.put(c, map.get(c)+1);
                } else{
                    map.put(c, 1);
                }
            }

            for(char c : ransomNote.toCharArray()){
                if(!map.containsKey(c) || map.get(c)<=0){
                    return false;
                }

                map.put(c, map.get(c)-1);
            }

            return true;
        }
    }

