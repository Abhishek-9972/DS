package ds.Array.a41WordBreak;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordBreak {
    public boolean wordBreak(String s, List<String> wordDict) {

        Map<Character, Integer> wordDictMap = new HashMap<>();
        for(String st: wordDict){
            for(char c : st.toCharArray())
                if(wordDictMap.containsKey(c)){
                    wordDictMap.put(c, wordDictMap.get(c)+1);
                } else {
                    wordDictMap.put(c, 1);
                }
        }

        System.out.println(wordDictMap);

        for(char c : s.toCharArray())
            if(wordDictMap.containsKey(c)){
                wordDictMap.put(c, wordDictMap.get(c)-1);
            } else {
                return false;
            }

        System.out.println(wordDictMap);


        for(Map.Entry<Character, Integer> m1 : wordDictMap.entrySet()){
            if(m1.getValue()<0){
                System.out.println(wordDictMap);
                return false;
            }
        }

        return true;
    }
}