package DS.Trie.a02AddAndSearchWordsDataStructure;

class TrieNode {
    TrieNode[] children;
    boolean isWordCompleted;

    TrieNode() {
        children = new TrieNode[26];
        isWordCompleted = false;
    }
}

public class AddAndSearchWords {
    TrieNode root;

    AddAndSearchWords() {
        root = new TrieNode();
    }

    void addWord(String word) {
        TrieNode newRoot = root;
        for (char ch : word.toCharArray()) {
            int alphabetIndex = ch - 'a';
            if (newRoot.children[alphabetIndex] == null) {
                newRoot.children[alphabetIndex] = new TrieNode();
            }
            newRoot = newRoot.children[alphabetIndex];
        }
        newRoot.isWordCompleted = true;
    }

    boolean searchHelper(String word, int index, TrieNode newRoot) {
        if (index == word.length())
            return newRoot.isWordCompleted;
        char ch = word.charAt(index);
        if (ch == '.') {
            for (int i = 0; i < 26; i++) {
                if (newRoot.children[i] != null && searchHelper(word, index + 1, newRoot.children[i])) {
                    return true;
                }
            }
            return false;
        }
        else {
            if (newRoot.children[ch - 'a'] == null) {
                return false;
            }
            return searchHelper(word, index + 1, newRoot.children[ch - 'a']);
        }
    }

    boolean search(String word) {
        return searchHelper(word, 0, root);
    }
}