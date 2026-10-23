package DS.Matrix.a06WordSearch2;

import java.util.ArrayList;
import java.util.List;

/**
 * https://leetcode.com/problems/word-search-ii/description/
 *
 * Pattern: Trie + DFS + Backtracking
 *
 * Difficulty: Hard
 *
 * Time Complexity:
 * Trie construction: O(W * L)
 * Board search: O(M * N * 3^L) worst-case after the first step,
 *               with Trie pruning reducing unnecessary exploration.
 *
 * Space Complexity: O(W * L) for the Trie + O(L) recursion stack.
 *
 * W = number of words
 * L = maximum word length
 * M = number of rows
 * N = number of columns
 */
public class WordSearch2 {

    class TrieNode {
        TrieNode[] children;
        boolean isEndOfWord;
        String word;

        TrieNode() {
            children = new TrieNode[26];
            isEndOfWord = false;
            word = null;
        }
    }

    private final TrieNode root;

    public WordSearch2() {
        root = new TrieNode();
    }

    public List<String> findWords(char[][] board, String[] words) {

        // Build Trie
        for (String word : words) {
            insert(word);
        }

        List<String> result = new ArrayList<>();

        // Start DFS from every cell
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                dfs(board, i, j, root, result);
            }
        }

        return result;
    }

    private void insert(String word) {

        TrieNode current = root;

        for (char ch : word.toCharArray()) {
            int index = ch - 'a';

            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }

            current = current.children[index];
        }

        current.isEndOfWord = true;
        current.word = word;
    }

    private void dfs(char[][] board, int i, int j,
                     TrieNode current, List<String> result) {

        // Boundary and visited check
        if (i < 0 || i >= board.length ||
                j < 0 || j >= board[i].length ||
                board[i][j] == ' ') {
            return;
        }

        char ch = board[i][j];
        int index = ch - 'a';

        // Current character is not a valid Trie path
        if (current.children[index] == null) {
            return;
        }

        // Move to the Trie node matching the current character
        current = current.children[index];

        // Complete word found
        if (current.isEndOfWord) {
            result.add(current.word);

            // Prevent duplicate result
            current.isEndOfWord = false;
        }

        // Mark current board cell as visited
        char temp = board[i][j];
        board[i][j] = ' ';

        // Explore four directions
        dfs(board, i + 1, j, current, result);
        dfs(board, i - 1, j, current, result);
        dfs(board, i, j + 1, current, result);
        dfs(board, i, j - 1, current, result);

        // Restore cell
        board[i][j] = temp;
    }
}
