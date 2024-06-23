package DS.String.a10PalindromicSubstrings;

public class PalindromicSubstrings {
    public int countSubstrings(String s) {
        int n = s.length();
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (isPalindrome(s, i, j) == true) {
                    count++;
                }
            }
        }
        return count;
    }

    int max = 0, start = 0, end = 0;

    public boolean isPalindrome(String s, int i, int j) {
        while (i < j) {
            char ch1 = s.charAt(i);
            char ch2 = s.charAt(j);
            if (ch1 != ch2)
                return false;
            i++;
            j--;
        }
        return true;
    }
}