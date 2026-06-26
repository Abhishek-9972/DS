package DS.Backtracking.a08LetterCombinationOfPhoneNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneNumber {

    Map<Character, String> digitToLetters = new HashMap<>();

    public PhoneNumber() {
        digitToLetters.put('2', "abc");
        digitToLetters.put('3', "def");
        digitToLetters.put('4', "ghi");
        digitToLetters.put('5', "jkl");
        digitToLetters.put('6', "mno");
        digitToLetters.put('7', "pqrs");
        digitToLetters.put('8', "tuv");
        digitToLetters.put('9', "wxyz");
    }

    public List<String> letterCombinations(String digits) {
        List<String> res = new ArrayList<>();
        if (digits == null || digits.isEmpty()) {
            return res;
        }
        backtrack(0, digits, new StringBuilder(), res);
        return res;
    }

    private void backtrack(int idx, String digits, StringBuilder partial, List<String> res) {
        if (idx == digits.length()) {
            res.add(partial.toString());
            return;
        }

        String letters = digitToLetters.get(digits.charAt(idx));
        for (char letter : letters.toCharArray()) {
            partial.append(letter);
            backtrack(idx + 1, digits, partial, res);
            partial.deleteCharAt(partial.length() - 1);
        }
    }
}