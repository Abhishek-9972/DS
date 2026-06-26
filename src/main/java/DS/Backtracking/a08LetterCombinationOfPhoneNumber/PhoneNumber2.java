package DS.Backtracking.a08LetterCombinationOfPhoneNumber;

import java.util.ArrayList;
import java.util.List;

public class PhoneNumber2 {

    String[] map = {
            "", "",
            "abc",
            "def",
            "ghi",
            "jkl",
            "mno",
            "pqrs",
            "tuv",
            "wxyz"
    };

    public List<String> letterCombinations(String digits) {

        List<String> result = new ArrayList<>();
        if (digits.isEmpty())
            return result;
        backtrack(0, digits, new StringBuilder(), result);
        return result;
    }

    private void backtrack(int idx, String digits, StringBuilder partial, List<String> result) {

        if (idx == digits.length()) {
            result.add(partial.toString());
            return;
        }

        String letters = map[digits.charAt(idx) - '0'];

        for (char ch : letters.toCharArray()) {
            partial.append(ch);
            backtrack(idx + 1, digits, partial, result);
            partial.deleteCharAt(partial.length() - 1);
        }
    }
}

