# Trie

## Implement Trie

### TrieNode
- `children = new TrieNode[26]`
- `boolean isEndOfWord`

### Insert
- `current = root`
- `current.children[index]`
- `current.isEndOfWord = true`

### Search
- Return `false` if child doesn't exist
- Return `current.isEndOfWord`

### StartsWith
- Return `false` if child doesn't exist
- Return `true`

### Word Dictionary
- Same `insert()` as Trie

---

# String

## Longest Substring Without Repeating Characters

- `Set<Character>`
- Iterate `right` from `0`
- If `set` contains current character
    - Keep removing characters from `left`
- Add current character to `set`
- `maxLength = right - left + 1`

---

## Reverse Polish Notation

- `Stack`

---

## Atoi (String to Integer)

- `trim()`
- Handle `+` / `-`
- `while (Character.isDigit(s.charAt(i)))`
- `digit = s.charAt(i) - '0'`
- `result = result * 10 + digit`
- Handle overflow
- `i++`

---

## Task Scheduler

- `charMap[ch - 'A']++`
- `Arrays.sort(charMap)`
- `maxVal = charMap[25] - 1`
- `idleSlots = maxVal * n`
- `idleSlots -= Math.min(maxVal, charMap[i])`

---

## Longest Palindromic Substring

- Expand Around Center
- `left`, `right`
- Update `start`
- `s.substring(start, start + maxLength)`

---

## Palindromic Substrings

- Expand Around Center
- Count all palindromes

# Stack

1. **Valid Parentheses**
    - Stack
    - Push when `(`
    - Pop when `)` and compare

2. **Min Stack**
    - `stack`
    - `minStack`

3. **Decode String**
    - `countStack`
    - `stringStack`
    - `currentNumber`
    - `StringBuilder currentString`
    - Digit → `currentNumber`
    - `[` → Push and reset
    - `]` → Pop and form `previousString`
    - Repeat and `previousString.append(currentString)`

4. **Stack Using Queue**
    - `queue`
    - `tempQueue`