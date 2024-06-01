package DS.Backtracking.a08LetterCombinationOfPhoneNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneNumber2 {
    public List<String> letterCombinations(String digits) {
        List<String> res = new ArrayList<>();

        if (digits == null || digits.length() == 0) {
            return res;
        }

        Map<Character, String> digitToLetters = new HashMap<>();
        digitToLetters.put('2', "abc");
        digitToLetters.put('3', "def");
        digitToLetters.put('4', "ghi");
        digitToLetters.put('5', "jkl");
        digitToLetters.put('6', "mno");
        digitToLetters.put('7', "pqrs");
        digitToLetters.put('8', "tuv");
        digitToLetters.put('9', "wxyz");

        backtrack(digits, new ArrayList<>(), 0, res, digitToLetters);

        return res;
    }

    private void backtrack(String digits, List<Character> partial, int idx, List<String> res, Map<Character, String> digitToLetters) {
        if (partial.size() == digits.length()) {
            String pRes = "";
            for(Character p: partial){pRes = pRes + p;}
            res.add(pRes);
            return;
        }

        String letters = digitToLetters.get(digits.charAt(idx));
        for (char letter : letters.toCharArray()) {
            partial.add(letter);
            backtrack(digits, partial, idx + 1, res, digitToLetters);
            partial.remove(partial.size()-1);
        }
    }
}