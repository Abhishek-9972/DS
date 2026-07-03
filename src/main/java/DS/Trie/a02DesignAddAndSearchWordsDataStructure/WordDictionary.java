package DS.Trie.a02DesignAddAndSearchWordsDataStructure;

/**
 * https://leetcode.com/problems/design-add-and-search-words-data-structure/
 */
public class WordDictionary {

    class TrieNode {

        TrieNode[] children;
        boolean isEndOfWord;

        TrieNode() {
            children = new TrieNode[26];
            isEndOfWord = false;
        }
    }

    private final TrieNode root;

    public WordDictionary() {
        root = new TrieNode();
    }

    // Insert a word
    public void addWord(String word) {

        TrieNode current = root;

        for (char ch : word.toCharArray()) {

            int index = ch - 'a';

            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }

            current = current.children[index];
        }

        current.isEndOfWord = true;
    }

    // Search a word (supports '.')
    public boolean search(String word) {
        return dfs(word, 0, root);
    }

    private boolean dfs(String word, int index, TrieNode current) {

        // Entire word processed
        if (index == word.length()) {
            return current.isEndOfWord;
        }

        char ch = word.charAt(index);

        // Wildcard '.'
        if (ch == '.') {

            for (TrieNode child : current.children) {

                if (child != null && dfs(word, index + 1, child)) {
                    return true;
                }
            }

            return false;
        }

        // Normal character
        int childIndex = ch - 'a';

        if (current.children[childIndex] == null) {
            return false;
        }

        return dfs(word, index + 1, current.children[childIndex]);
    }
}