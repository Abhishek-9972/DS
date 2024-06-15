package DS.DP.MISC.a06WordBreak;

import java.util.Arrays;
import java.util.List;

public class WordBreak {
        public boolean wordBreak(String s, List<String> wordDict) {
            boolean[] dp=new boolean[s.length()+1];
            dp[0]=true;
            for(int i=1;i<=s.length();i++){
                for(int j=0;j<i;j++){
                    if(wordDict.contains(s.substring(j,i)) && dp[j]) dp[i]=true;
                }
            }
            return dp[s.length()];
        }

    public static void main(String[] args) {
        String s = "applepenapple";
        List<String> wordDict = Arrays.asList("apple","pen");
        WordBreak wordBreak = new WordBreak();
        wordBreak.wordBreak(s, wordDict);
    }
}