package DS.SlidingWindow.a07LongestRepeatingCharacterReplacement;

public class LongestRepeatingCharacterReplacement {
    public int characterReplacement(String s, int k) {
        int n = s.length();
        int[] charCounts = new int[26];

        int windowStart = 0;
        int maxLength = 0;
        int maxCount = 0;

        for (int windowEnd = 0; windowEnd < n; windowEnd++) {
            charCounts[s.charAt(windowEnd) - 'A']++;
            int currentCharCount = charCounts[s.charAt(windowEnd) - 'A'];
            maxCount = Math.max(maxCount, currentCharCount);

            while (windowEnd - windowStart - maxCount + 1 > k) {
                charCounts[s.charAt(windowStart) - 'A']--;
                windowStart++;
            }

            maxLength = Math.max(maxLength, windowEnd - windowStart + 1);
        }
        return maxLength;
    }
}