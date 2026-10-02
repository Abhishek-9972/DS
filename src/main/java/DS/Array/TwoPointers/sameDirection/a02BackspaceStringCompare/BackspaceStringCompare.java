package DS.Array.TwoPointers.sameDirection.a02BackspaceStringCompare;

/**
 * https://algomaster.io/learn/dsa/backspace-string-compare
 * Time Complexity: O(n + m).
 * Space Complexity: O(1).
 */
public class BackspaceStringCompare {
    public boolean backspaceCompare(String s, String t) {
        int i = s.length() - 1;
        int j = t.length() - 1;

        int skipS = 0;
        int skipT = 0;

        while (i >= 0 || j >= 0) {

            // Find next valid character in s
            while (i >= 0) {
                if (s.charAt(i) == '#') {
                    skipS++;
                    i--;
                } else if (skipS > 0) {
                    skipS--;
                    i--;
                } else {
                    break;
                }
            }

            // Find next valid character in t
            while (j >= 0) {
                if (t.charAt(j) == '#') {
                    skipT++;
                    j--;
                } else if (skipT > 0) {
                    skipT--;
                    j--;
                } else {
                    break;
                }
            }

            // Both have no more valid characters
            if (i < 0 && j < 0) {
                return true;
            }

            // One has a character, other doesn't
            if (i < 0 || j < 0) {
                return false;
            }

            // Compare valid characters
            if (s.charAt(i) != t.charAt(j)) {
                return false;
            }

            i--;
            j--;
        }

        return true;
    }
}
