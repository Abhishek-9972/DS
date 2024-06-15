package DS.Array.a41FirstUniqueCharacter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FirstUniqueCharacter {
    public int firstUniqChar(String s) {
        HashMap<Character,Integer> map = new LinkedHashMap<>();
        for(int i=0; i<s.length(); i++){
            char c = s.charAt(i);
            if(!map.containsKey(c)){
                map.put(c,i);
            }
            else{
                map.put(c,-1);
            }
        }

        System.out.println(map);

        for(Map.Entry<Character,Integer> m1 : map.entrySet()){
            if(m1.getValue()>-1){
                return m1.getValue();
            }
        }
        return -1;
    }
}