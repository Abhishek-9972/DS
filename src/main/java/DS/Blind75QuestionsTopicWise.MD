# Blind 75 — Topic-wise Notes

Questions are grouped in the same topic order as the previous `Blind75.md`. Original Java paths and study notes from `Blind75Questions.MD` are preserved where available.

## 1. Array

### Hashing

#### [TwoSum](Array/Hashing/a01TwoSum/TwoSum.java)
- **Pattern:** Unsorted array — HashMap.
- **Note:** `map.put(nums[i], i)` → check `map.containsKey(target - nums[i])`.

#### [MissingNumber](Array/Hashing/a02MissingNumberInArray/MissingNumber.java)
- **Example:** `nums = [3,0,1]` → output `2`.
- **Note:** `n * (n + 1) / 2 - sum`.

#### [Longest Consecutive Subsequence](Array/Hashing/a08LongestConsecutiveSequence/LongestConsecutiveSequence.java)
- **Example:** `nums = [100,4,200,1,3,2]` → output `4`.
- **Explanation:** The longest consecutive sequence is `[1, 2, 3, 4]`.
- **Remember:** `if (!set.contains(num - 1))`.

#### [ContainsDuplicate](Array/Hashing/a09ContainsDuplicate/ContainsDuplicate.java)
- **Example:** `[1,2,3,1]` → `true`.
- **Note:** `if (!set.add(num)) { return true; }`.

#### [GroupAnagrams](Array/Hashing/a11GroupAnagrams/GroupAnagrams.java)

### Two Pointers

#### [TwoSum2](Array/TwoPointers/OppositeDirection/a01TwoSum02/TwoSum02.java) — **NB75**
- **Pattern:** Sorted array — two pointers.
- **Note:** `nums[start] + nums[end] == target`.

#### [ContainerWithMostWater](Array/TwoPointers/OppositeDirection/a02ContainerWithMostWater/ContainerWithMostWater.java)
- **Example:** `height = [1,8,6,2,5,4,8,3,7]` → output `49`.
- **Note:** `max = Math.max(max, height[aPointer] * (bPointer - aPointer));`.

#### [ThreeSum](Array/TwoPointers/OppositeDirection/a07ThreeSum/ThreeSum.java)
- **Example:** `nums = [-1,0,1,2,-1,-4]` → `[[-1,-1,2],[-1,0,1]]`.

#### [ThreeSumClosest](Array/TwoPointers/OppositeDirection/a08ThreeSumClosest/ThreeSumClosest.java) — **NB75**
- **Example:** `nums = [-1,2,1,-4], target = 1` → output `2`.

#### [FourSum](Array/TwoPointers/OppositeDirection/a09FourSum/FourSum.java) — **NB75**
- **Example:** `nums = [1,0,-1,0,-2,2], target = 0` → `[[-2,-1,1,2],[-2,0,0,2],[-1,0,0,1]]`.

**Shared notes for ThreeSum / ThreeSumClosest / FourSum**
```java
Arrays.sort(arr);
for (int i = 0; i < nums.length - 2; i++) {
    l = i + 1;
    r = nums.length - 1;
    while (l < r) {
        // Compare sum with target and move pointers.
    }
}
```

### Kadane's Algorithm

#### [MaximumProductSubArray](Array/Kadane/a01MaximumProductSubarray/MaximumProductSubArray.java)
- **Example:** `nums = [2,3,-2,4]` → output `6`.
- **Explanation:** `[2,3]` has the largest product.
- **Note:** `l *= nums[i]; r *= nums[n - 1 - i]; max = Math.max(max, Math.max(l, r));`.

#### [MaximumSumSubArray](Array/Kadane/a02MaximumSubArray/KadaneUsingDP.java)
- **Example:** `nums = [-2,1,-3,4,-1,2,1,-5,4]` → output `6`.
- **Explanation:** `[4,-1,2,1]` has the largest sum.
- **Note:** `dp[0] = 0; for(i=1->n) dp[i] = Math.max(nums[i], nums[i] + dp[i - 1]); max = Math.max(max, dp[i])`.

### Binary Search

#### [SearchInRotatedSortedArray](Array/BinarySearch/a03SearchInRotatedSortedArray/SearchInRotatedSortedArray.java)
- **Example:** `nums = [4,5,6,7,0,1,2], target = 0` → output `4`.
- **Note:** Identify the sorted half. Example branch:
```java
else if (nums[st] <= nums[mid]) {
    if (nums[st] <= target && nums[mid] > target) {
        end = mid - 1;
    }
}
```

#### [MinimumInRotateSortedArray](Array/BinarySearch/a04MinimumInSortedArray/MinimumInSortedArray.java)
- **Example:** `nums = [3,4,5,1,2]` → output `1`.
- **Note:**
```java
if (nums[mid] < nums[end]) {
    end = mid;
} else {
    st = mid + 1;
}
return nums[st];
```

### Prefix Sum

#### [ProductOfArrayExceptSelf](Array/PrefixSum/a02ProductOfArrayExceptSelf/ProductOfArrayExceptSelf.java)
- **Example:** `nums = [1,2,3,4]` → output `[24,12,8,6]`.
- **Note:** Left pass: `result[i] = result[i - 1] * nums[i - 1]`; right pass: `result[i] *= rightProduct`.

### Greedy

#### [JumpGame](Array/Greedy/a01Jump1/Jump1.java)
- **Example:** `nums = [2,3,1,1,4]` → output `true`.
- **Explanation:** Jump from index `0` to `1`, then jump to the last index.
- **Note:** `if (i > reachable) return false; reachable = Math.max(reachable, i + nums[i]);`.

#### [BestTimeToBuyAndSellStock](Array/Greedy/a02Stock1/Stock1.java)
- **Example:** `prices = [7,1,5,3,6,4]` → output `5`.
- **Explanation:** Buy at `1`, sell at `6`, profit `5`.
- **Note:** `if (prices[i] < min) { min = prices[i]; } else { max = Math.max(max, prices[i] - min); }`.

## 2. String

#### [ValidPalindrome](String/a01ValidPalindrome/ValidPalindrome.java)
- **Note:** Compare characters case-insensitively, moving inward:
```java
if (Character.toLowerCase(s.charAt(left)) !=
    Character.toLowerCase(s.charAt(right))) {
    return false;
}
```

#### [ValidAnagram](String/a02ValidAnagram/ValidAnagram.java)
- **Example:** `s = "anagram", t = "nagaram"` → output `true`.
- **Note:** Put characters in a map and check frequency counts.

#### [LongestSubstringWithoutRepeatingCharacters](String/a05LongestSubstringWithoutRepeatingNumbers/LongestSubstringWithoutRepeatingCharacters.java)
- **Example:** `s = "abcabcbb"` → output `3`; answer is `"abc"`.
- **Note:**
```java
for (int right = 0; right < s.length(); right++) {
    while (charSet.contains(s.charAt(right))) {
        // Shrink the window from the left.
    }
}
```

#### EncodeAndDecodeString
- Listed in the source notes; no implementation notes provided.

#### [LongestPalindromicSubstring](String/a09LongestPalindromicSubstring/LongestPalindromicSubstring.java)
- **Example:** `s = "babad"` → output `"bab"`.

#### [PalindromicSubstrings](String/a10PalindromicSubstrings/PalindromicSubstrings.java)
- **Examples:** `"abc"` → `3`; `"aaa"` → `6`.
- **Shared note for palindrome problems:**
```java
expandAroundCenter(s, i, i);
expandAroundCenter(s, i, i + 1);
return s.substring(start, start + maxLength);
```

## 3. Sliding Window

#### [LongestRepeatingCharacterReplacement](SlidingWindow/a07LongestRepeatingCharacterReplacement/LongestRepeatingCharacterReplacement.java)
- **Example:** `s = "AABABBA", k = 1` → output `4`.
- **Explanation:** Replace one character to make the longest repeating substring `"BBBB"`.
- **Notes:** Track character frequencies, `maxFreq`, and window length. If `windowLength - maxFreq > k`, decrement the outgoing character's frequency and move `left` forward.

## 4. Linked List

#### [RemoveNthNodeFromEndOfList](LinkedList/a02RemoveNthNodeFromEndOfList/RemoveNode.java)
- **Example:** `head = [1,2,3,4,5], n = 2` → output `[1,2,3,5]`.
- **Note:** Calculate length, then remove the node at index `length - n - 1`:
```java
if (i == length - n - 1) {
    current.next = current.next.next;
    break;
}
current = current.next;
```

#### [MergeTwoSortedLists](LinkedList/a03MergeTwoSortedLists/MergeTwoSortedList.java)
- **Note:** Use a `dummyHead` and `current`; iterate while `l1 != null && l2 != null`.

#### [MergeKSortedLists](LinkedList/a04MergeKLists/MergeKLists.java)
- **Note:** Use a min-heap:
```java
PriorityQueue<ListNode> pq =
    new PriorityQueue<>((a, b) -> a.val - b.val);
```
- Use `dummyHead` and `current`; poll the smallest node and attach it using `current.next = smallest`.

#### [LinkedListCycle](LinkedList/a09LinkedListCycle/LinkedListCycle.java)
#### [LinkedListCycle2](LinkedList/a10LinkedListCycle2/LinkedListCycle2.java) — **NB75**
- **Example:** `head = [3,2,0,-4], pos = 1` → output `true`.
- **Note:** Fast/slow pointers. A loop condition from the source:
```java
while (fastPtr.next != null && fastPtr.next.next != null) {
    // Move pointers.
}
```

#### [ReverseLinkedList](LinkedList/a12ReverseLinkedList/ReverseLinkedList.java)
- Listed in the previous topic list; no implementation notes provided in this source.

#### [ReorderList](LinkedList/a19ReorderList/ReorderList.java)
- **Example:** `head = [1,2,3,4]` → output `[1,4,2,3]`.
- **Note:** Find the middle, reverse the second half, then interleave:
```java
first = head;
middle = middle(head);
second = reverse(middle.next);
// While second != null: save nextFirst and nextSecond, then interleave.
```

## 5. Stack

#### [ValidParentheses](Stack/a01ValidParantheses/ValidParanthesis.java)
- **Example:** `s = "()[]{}"` → output `true`.
- **Note:** Use `Stack<Character> stack = new Stack<>();` and push opening brackets.

## 6. Binary Tree / BST

### Binary Tree

#### [MaximumDepthOfBinaryTree](BinaryTree/a02MaximumDepthOfBinaryTree/MaximumDepthOfBinaryTree.java)
- **Note:**
```java
if (root == null) return 0;
int left = maxDepth(root.left);
int right = maxDepth(root.right);
return 1 + Math.max(left, right);
```

#### [SameTree](BinaryTree/a03SameTree/SameTree.java)
- Listed in the previous topic list; no implementation notes provided in this source.

#### [BinaryTreeLevelOrderTraversal](BinaryTree/a05BinaryTreeLevelOrderTraversal/BinaryTreeLevelOrderTraversal.java)
- **Note:** Use a queue; process one level at a time by capturing `int size = queue.size()` before iterating through that level.

#### [InvertBinaryTree](BinaryTree/a17InvertBinaryTree/InvertBinaryTree.java)
- **Note:**
```java
TreeNode left = invertTree(root.left);
TreeNode right = invertTree(root.right);
root.left = right;
root.right = left;
```

#### [BinaryTreeFromPreOrderAndInOrder](BinaryTree/a22BinaryTreeFromPreorderInorder/BinaryTreeFromPreorderInorder.java)
- **Notes:**
```java
root = new TreeNode(preOrder[preStart]);
root.left = helper(preStart + 1, inStart, inIndex - 1, preOrder, inOrder);
root.right = helper(preStart + inIndex - inStart + 1, inIndex + 1, inEnd, preOrder, inOrder);
```

#### [BinaryTreeMaxPathSum](BinaryTree/a25BinaryTreeMaxPathSum/BinaryTreeMaxPathSum.java)
- **Example:** `root = [1,2,3]` → output `6`.
- **Note:**
```java
int left = Math.max(helper(root.left), 0);
int right = Math.max(helper(root.right), 0);
max = Math.max(max, root.val + left + right);
return root.val + Math.max(left, right);
```

#### [SerializeAndDeserializeBinaryTree](BinaryTree/a26SerializeAndDeserializeTree/SerializeAndDeserializeTree.java)
- **Serialize:** Use `Queue<TreeNode> queue = new LinkedList<>();` and `StringBuilder res = new StringBuilder();`.
- **Deserialize:** Use `String[] values = res.split(" ");`, start `index = 1`, and retrieve each parent using `queue.poll()`.

#### [SubTreeOfAnotherTree](BinaryTree/a27SubTreeOfAnotherTree/SubTreeOfAnotherTree.java)
- **Note:** Check whether the current tree matches the subtree, then recurse through the main tree.
```java
sameTree(root, subRoot);
sameTree(root.left, subRoot.left);
sameTree(root.right, subRoot.right);
```
- Same-tree check:
```java
s.val == t.val &&
isSameTree(s.left, t.left) &&
isSameTree(s.right, t.right)
```

### Binary Search Tree (BST)

#### [ValidateBinarySearchTree](BinaryTree/a09ValidateBinarySearchTree/ValidateBinarySearchTree.java)
- **Example:** `root = [2,1,3]` → output `true`.
- **Note:** Start with `isValidBST(root, Long.MIN_VALUE, Long.MAX_VALUE)` and recursively narrow the valid range:
```java
isBST(root.left, min, root.val) &&
isBST(root.right, root.val, max)
```

#### [KthSmallestElementInBST](BinaryTree/a11KthSmallestElementInBST/KthSmallestElementInBST.java)
- **Example:** `root = [5,3,6,2,4,null,null,1], k = 3` → output `3`.
- **Note:** In-order traversal; return a found result from the left subtree, increment `count`, check `count == k`, then recurse right.

#### [LCAofBST](BinaryTree/a12LCAofBST/LCAofBST.java)
- **Note:** If both target values are less than the root, recurse left; if both are greater, recurse right.

## 7. Graph

#### AlienDictionary
- Listed in the source notes; no implementation notes provided.

#### [NumberOfIslands](Matrix/a05NumberOfIslands/NumberOfIslands.java)
- **Note:** Count each island by adding the result of DFS: `res = res + dfs(grid, i, j);`.

## 8. Matrix

#### [RotateImage](Matrix/a01RotateImage/RotateImage.java)

#### [SpiralMatrix](Matrix/a02SprialMatrix/SpiralMatrix.java)
- **Note:** Initialize `rs = 0`, `cs = 0`, `re = arr.length - 1`, `ce = arr[0].length - 1`; iterate while `rs <= re && cs <= ce`.

#### Pacific Atlantic Waterflow
- Listed in the previous topic list, but not present in the attached notes.

#### [WordSearch](Matrix/a06WordSearch/WordSearch.java)
- **Example:** Board `[['A','B','C','E'],['S','F','C','S'],['A','D','E','E']]`, word `"ABCCED"` → output `true`.
- **Note:** Start DFS from matching first characters; stop successfully when `count == word.length`; fail when the board character does not match the current character.

#### [WordSearch2](Matrix/a06WordSearch2/WordSearch2.java)
- **Example:** Board `[['o','a','a','n'],['e','t','a','e'],['i','h','k','r'],['i','f','l','v']]`, words `["oath","pea","eat","rain"]` → output `["eat","oath"]`.
- **Note:** Insert words into a Trie, then DFS through the board using the Trie node.

#### [Set Matrix Zeroes](Matrix/a08SetMatrixZeroes/SetMatrixZeroes.java)
- **Example:** `[[1,1,1],[1,0,1],[1,1,1]]` → `[[1,0,1],[0,0,0],[1,0,1]]`.
- **Note:** Record zero-containing rows and columns: `if (matrix[i][j] == 0) { rows.add(i); columns.add(j); }`.

## 9. Dynamic Programming (DP)

### General DP

#### [ClimbingStairs](DP/MISC/a03ClimbingStairs/ClimbingStairs.java)
- **Note:** Base cases `dp[0] = 1`, `dp[1] = 1`; recurrence `dp[i] = dp[i - 1] + dp[i - 2]`.

#### [HouseRobber](DP/MISC/a04HouseRobber01/HouseRobber1.java)
- **Note:** `dp[0] = nums[0]`; `dp[1] = Math.max(nums[0], nums[1])`; recurrence `dp[i] = Math.max(dp[i - 1], nums[i] + dp[i - 2])`.

#### [HouseRobber2](DP/MISC/a05HouseRobber02/HouseRobber2.java)
- **Note:** Solve two cases, skipping the first house and skipping the last house; return `Math.max(lootSkippingLast, lootSkippingFirst)`.

#### [UniquePaths](DP/MISC/a06UniquePaths/b3UniquePaths.java)
- **Note:** Initialize `dp[i][0] = 1` and `dp[0][j] = 1`; recurrence `dp[i][j] = dp[i - 1][j] + dp[i][j - 1]`; return `dp[m - 1][n - 1]`.

#### [WordBreak](DP/MISC/a09WordBreak/WordBreak.java)
- **Example:** `s = "leetcode", wordDict = ["leet","code"]` → output `true`.
- **Note:** `if (dp[j] && words.contains(s.substring(j, i)))`.

#### [LongestIncreasingSubsequence](DP/MISC/a02LongestIncreasingSubsequence/LongestIncreasingSubsequence.java)
- **Example:** `nums = [10,9,2,5,3,7,101,18]` → output `4`.
- **Explanation:** One LIS is `[2,3,7,101]`.

### Knapsack / Unbounded Knapsack

#### [CoinChange](DP/knapsack/unbounded/coinChange/coinChange1/MinNoOfCoins.java)
- **Example:** `coins = [1,2,5], amount = 11` → output `3` (`5 + 5 + 1`).
- **Note from source:**
```java
dp[0][i] = Integer.MAX_VALUE - 1;
dp[i][j] = Math.min(1 + dp[i][j - arr[i - 1]], dp[i - 1][j]);
```

### Longest Common Subsequence (LCS)

#### [LongestCommonSubSequence](DP/LCS/a01longestCommonSubsequence/LongestCommonSubsequence.java)
- Listed in the previous topic list; no implementation notes provided in this source.

## 10. Backtracking

#### [CombinationSum](Backtracking/a06CombinationSum1/CombinationSum1.java)
- **Example:** `candidates = [2,3,6,7], target = 7` → `[[2,2,3],[7]]`.
- **Note:**
```java
combinationSum(
    int start,
    int remain,
    int[] input,
    List<Integer> partial,
    List<List<Integer>> result
) {
    if (remain == 0) {
        result.add(new ArrayList<>(partial));
    }
}
```

## 11. Trie

#### [ImplementTrie](Trie/a01ImplementTrie/Trie.java)
- **Operations:** `insert(word)`, `search`, `startsWith`.
- **Node:** `TrieNode[] children = new TrieNode[26]; boolean isEndOfWord;`.
- **Note:** Start from the root; for each character, calculate `index = ch - 'a'`, initialize a missing child, then advance `current`. The source notes emphasize the null check, initialization, and advancing to the next node.

#### [AddAndSearchWordsDataDictionary](Trie/a02DesignAddAndSearchWordsDataStructure/WordDictionary.java)
- **Example operations:** Add `"bad"`, `"dad"`, `"mad"`; searches for `"pad"`, `"bad"`, `".ad"`, `"b.."` return `false`, `true`, `true`, `true`.
- **Note:** DFS with `(word, index, current)`. Return true when `index == word.length()`. For `'.'`, try each child; otherwise follow the matching child if present.

## 12. Heap / Priority Queue

#### [TopKFrequentElements](Heap/a05topKFrequentNumbers/TopKFrequentNumbers.java)

#### [FindMedianOfTwoDataStream](Heap/a08findMedianOfDataStream/MedianOfDataStream.java)
- **Example:** Add `1`, add `2`, find median → `1.5`; add `3`, find median → `2.0`.
- **Add number:** Balance two heaps so their sizes differ by at most one:
```java
if (even) {
    minHeap.offer(num);
    maxHeap.offer(minHeap.poll());
} else {
    maxHeap.offer(num);
    minHeap.offer(maxHeap.poll());
}
```
- **Find median:**
```java
return even
    ? (minHeap.peek() + maxHeap.peek()) / 2.0
    : maxHeap.peek();
```

## 13. Intervals

#### [MergeIntervals](Intervals/a02MergeIntervals/MergeIntervals.java)
- **Example:** `[[1,3],[2,6],[8,10],[15,18]]` → `[[1,6],[8,10],[15,18]]`.
- **Note:** Sort by start:
```java
Arrays.sort(arr, (a, b) -> Integer.compare(a[0], b[0]));
```
- Merge overlapping intervals using the end of the current interval; otherwise add the interval to the result.

#### [InsertIntervals](Intervals/a03InsertIntervals/InsertIntervals.java)
- **Example:** `intervals = [[1,2],[3,5],[6,7],[8,10],[12,16]]`, `newInterval = [4,8]` → `[[1,2],[3,10],[12,16]]`.
- **Note:** Create a new array with one extra slot, copy existing intervals, and append `newInterval`.

#### Additional interval note (source label/path mismatch)
- The source labels this entry `InsertIntervals` but links to `Intervals/a02MergeIntervals/MergeIntervals.java`.
- **Example:** `intervals = [[1,3],[6,9]]`, `newInterval = [2,5]` → `[[1,5],[6,9]]`.
- **Note:** `int[][] modifiedIntervals = new int[intervals.length + 1][2];`.

#### [MeetingRooms1](Intervals/a04MeetingRooms1/MeetingRooms1.java)
- **Example:** `[[0,30],[5,10],[15,20]]` → `false`.
- **Note:** After sorting by start time, if `intervals[i][0] < intervals[i - 1][1]`, return false.

#### [NonOverlappingIntervals](Intervals/a06NonOverlappingIntervals/NonOverlappingIntervals.java)

## 14. Bit Manipulation

#### ReverseBits

#### NumberOf1Bits

## 15. Other / Unclassified

- `img.png` — backup image from the previous list; not a question.
