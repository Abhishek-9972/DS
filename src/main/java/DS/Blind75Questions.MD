Blind 75

[Longest Consecutive Subsequence](Array/Hashing/a08LongestConsecutiveSequence/LongestConsecutiveSequence.java)
````
Input: nums = [100,4,200,1,3,2] Output: 4 Explanation: The longest consecutive elements sequence is [1, 2, 3, 4]. Therefore its length is 4.
Remember - if (!set.contains(num - 1))
````

[TwoSum](Array/Hashing/a01TwoSum/TwoSum.java)
[TwoSum2](Array/TwoPointers/OppositeDirection/a01TwoSum02/TwoSum02.java)
````
TwoSum - Unsorted - map.put(nums[i],i) -> map.contains(target-nums[i])
TwoSum2 - Sorted - TwoPointers - nums[start] + nums[end] == target
````

[LongestSubstringWithoutRepeatingCharacters](String/a05LongestSubstringWithoutRepeatingNumbers/LongestSubstringWithoutRepeatingCharacters.java)
````
Input: s = "abcabcbb" Output: 3 Explanation: The answer is "abc"
for (int right = 0; right < s.length(); right++) {
    while (charSet.contains(s.charAt(right))) {
````

[LongestPalindromicSubstring](String/a09LongestPalindromicSubstring/LongestPalindromicSubstring.java)
[PalindromicSubstrings](String/a10PalindromicSubstrings/PalindromicSubstrings.java)
````
LongestPalindromicSubstring - s = "babad" - Output - bab
PalindromicSubstrings - abc - 3 - aaa - 6

expandAroundCenter(s,i,i); expandAroundCenter(s,i,i+1); return s.substring(start, start + maxLength);
````

[ContainerWithMostWater](Array/TwoPointers/OppositeDirection/a02ContainerWithMostWater/ContainerWithMostWater.java)
````
Input: height = [1,8,6,2,5,4,8,3,7] Output: 49
max = Math.max(max, height[aPointer] * (bPointer - aPointer));
````

[WordBreak](DP/MISC/a09WordBreak/WordBreak.java)
````
Input: s = "leetcode", wordDict = ["leet","code"] Output: true
if (dp[j] && words.contains(s.substring(j, i))) {
````

[LinkedListCycle](LinkedList/a09LinkedListCycle/LinkedListCycle.java)
[LinkedListCycle2](LinkedList/a10LinkedListCycle2/LinkedListCycle2.java)
````
Input: head = [3,2,0,-4], pos = 1 Output: true
while (fastPtr.next != null && fastPtr.next.next != null) {
````

[MissingNumber](Array/Hashing/a02MissingNumberInArray/MissingNumber.java)
````
Input: nums = [3,0,1] Output: 2
n * (n+1)/2 - sum
````

[ThreeSum](Array/TwoPointers/OppositeDirection/a07ThreeSum/ThreeSum.java)
[ThreeSumClosest](Array/TwoPointers/OppositeDirection/a08ThreeSumClosest/ThreeSumClosest.java)
[FourSum](Array/TwoPointers/OppositeDirection/a09FourSum/FourSum.java)

````
3 Sum - Input: nums = [-1,0,1,2,-1,-4] Output: [[-1,-1,2],[-1,0,1]]
3 Sum Closest - Input: nums = [-1,2,1,-4], target = 1 Output: 2
4 Sum - Input: nums = [1,0,-1,0,-2,2], target = 0 Output: [[-2,-1,1,2],[-2,0,0,2],[-1,0,0,1]]

Arrays.sort(arr);
for (int i=0; i<nums.length-2; i++) -> l = i+1; r = nums.length-1; -> while (l<r) -> if(sum==0)
````

[ReorderList](LinkedList/a19ReorderList/ReorderList.java)
````
Input: head = [1,2,3,4] Output: [1,4,2,3]
First = head, middle = middle(head), second = reverse(middle.next) ; middle.next!=null; while(second!=null) {nextFirst, nextSecond}
````

AlienDictionary
EncodeAndDecodeString

[RemoveNthNodeFromEndOfList](LinkedList/a02RemoveNthNodeFromEndOfList/RemoveNode.java)
````
Input: head = [1,2,3,4,5], n = 2 , Output: [1,2,3,5]
Length ; if(i==length-n-1) {current.next = current.next.next; break;} current = current.next
````

[ValidParentheses](Stack/a01ValidParantheses/ValidParanthesis.java)
````
Input: s = "()[]{}" Output: true
Stack<Character> stack = new Stack<>(); stack.push(ch)
````

[MergeTwoSortedLists](LinkedList/a03MergeTwoSortedLists/MergeTwoSortedList.java)
````
DummyHead, current=DummyHead while(l1!=null & l2!=null)
````

[MergeKSortedLists](LinkedList/a04MergeKLists/MergeKLists.java)
````
PriorityQueue<ListNode>((a,b) -> a.val - b.val);  DummyHead, current=DummyHead ; smallest = pq.poll; current.next= smallest
````

[MaximumProductSubArray](Array/Kadane/a01MaximumProductSubarray/MaximumProductSubArray.java)
````
Input: nums = [2,3,-2,4] Output: 6 Explanation: [2,3] has the largest product 6.
l *= nums[i]; r *= nums[n - 1 - i]; max = Math.max(max, Math.max(l, r));
````

[MinimumInRotateSortedArray](Array/BinarySearch/a04MinimumInSortedArray/MinimumInSortedArray.java)
````
Input: nums = [3,4,5,1,2] Output: 1
if (nums[mid] < nums[end]) {end = mid;} else {st = mid + 1;} return nums[st];
````

[SearchInRotatedSortedArray](Array/BinarySearch/a03SearchInRotatedSortedArray/SearchInRotatedSortedArray.java)
````
Input: nums = [4,5,6,7,0,1,2], target = 0 Output: 4
else if (nums[st] <= nums[mid]) {if (nums[st] <= target && nums[mid] > target) {end = mid - 1;} 
````

[CombinationSum](Backtracking/a06CombinationSum1/CombinationSum1.java)
````
Input: candidates = [2,3,6,7], target = 7 Output: [[2,2,3],[7]]
combinationSum(int start, int remain, int [] input, List<Integer> partial, List<List<Integer>> result) { if(remain==0) { result.add(new ArrayList<>(partial)); }
````

[FindMedianOfTwoDataStream](Heap/a08findMedianOfDataStream/MedianOfDataStream.java)
````
Input - ["MedianFinder", "addNum", "addNum", "findMedian", "addNum", "findMedian"]   -   [[], [1], [2], [], [3], []]   Output  - [null, null, null, 1.5, null, 2.0]
Add - if (even) {this.minHeap.offer(num);this.maxHeap.offer(this.minHeap.poll());} else {this.maxHeap.offer(num);this.minHeap.offer(this.maxHeap.poll());}
FindMedian -        return even ? (this.minHeap.peek() + this.maxHeap.peek()) / 2.0 : this.maxHeap.peek();
````

[SerializeAndDeserializeBinaryTree](BinaryTree/a26SerializeAndDeserializeTree/SerializeAndDeserializeTree.java)
````
Serailize - Queue<TreeNode> queue = new LinkedList<>(); StringBuilder res = new StringBuilder();
Deserialize - String [] values = res.split(" "); int index = 1; TreeNode parent = queue.poll();
````

[LongestRepeatingCharacterReplacement](SlidingWindow/a07LongestRepeatingCharacterReplacement/LongestRepeatingCharacterReplacement.java)
````
Input: s = "AABABBA", k = 1 Output: 4 Explanation: Replace the one 'A' in the middle with 'B' and form "AABBBBA". The substring "BBBB" has the longest repeating letters, which is 4.
for(int right=0; right<s.length-1; right++) { freqMap(s.charAt(right)-'A')++; maxFreq; windowLenth; if (windowLength-maxFreq > k) {freqMap(s.charAt(left)-'A')--; left++;} windowLength; maxLength;
````

[LongestIncreasingSubsequence](DP/MISC/a02LongestIncreasingSubsequence/LongestIncreasingSubsequence.java)
````
Input: nums = [10,9,2,5,3,7,101,18] Output: 4 Explanation: The longest increasing subsequence is [2,3,7,101], therefore the length is 4.

````

[RotateImage](Matrix/a01RotateImage/RotateImage.java)

[GroupAnagrams](Array/Hashing/a11GroupAnagrams/GroupAnagrams.java)

[NonOverlappingIntervals](Intervals/a06NonOverlappingIntervals/NonOverlappingIntervals.java)

[MaximumSumSubArray](Array/Kadane/a02MaximumSubArray/KadaneUsingDP.java)
````
Input: nums = [-2,1,-3,4,-1,2,1,-5,4] Output: 6 Explanation: The subarray [4,-1,2,1] has the largest sum 6.
dp[i] = Math.max(nums[i], nums[i] + dp[i - 1]);
````

[SpiralMatrix](Matrix/a02SprialMatrix/SpiralMatrix.java)
````
rs = 0; cs = 0; re = arr.length-1; ce = arr[0].length-1 -> while(rs<=re && cs<=ce) { }
````

[JumpGame](Array/Greedy/a01Jump1/Jump1.java)
````
Input: nums = [2,3,1,1,4] Output: true Explanation: Jump 1 step from index 0 to 1, then 3 steps to the last index.
if (i > reachable) return false; reachable = Math.max(reachable, i + nums[i]);
````

[MergeIntervals](Intervals/a02MergeIntervals/MergeIntervals.java)
````
Input: intervals = [[1,3],[2,6],[8,10],[15,18]] Output: [[1,6],[8,10],[15,18]] Explanation: Since intervals [1,3] and [2,6] overlap, merge them into [1,6].
Arrays.sort(arr, (a,b) -> Integer.compare(a[0], b[0]));
currentInterval = intervals[i]; topInterval = stack.peek(); if(topEle[1]>curr[0]{top[1] = Math.max(curr[1], top[1]);} else {stack.push(curr);}
````

[InsertIntervals](Intervals/a02MergeIntervals/MergeIntervals.java)
````
Input: intervals = [[1,3],[6,9]], newInterval = [2,5] Output: [[1,5],[6,9]]
int[][] modifiedIntervals = new int[intervals.length + 1][2];
````

[SubTreeOfAnotherTree](BinaryTree/a27SubTreeOfAnotherTree/SubTreeOfAnotherTree.java)
````
subTree - sameTree(root, subRoot); return sameTree(root.left, subRoot.left); return sameTree(root.right, subRoot.right)
sameTree return s.val == t.val && isSameTree(s.left, t.left) && isSameTree(s.right, t.right)
````

[UniquePaths](DP/MISC/a06UniquePaths/b3UniquePaths.java)
```
dp[i][0] = 1; dp[0][j] = 1;   ---->   dp[i][j] = dp[i-1][j] + dp[i][j-1]  -> return dp[m-1][n-1]
```

ReverseBits

NumberOf1Bits

[CoinChange](DP/knapsack/unbounded/coinChange/coinChange1/MinNoOfCoins.java)
````
Input: coins = [1,2,5], amount = 11 Output: 3 Explanation: 11 = 5 + 5 + 1
dp[0][i] = Integer.MAX_VALUE - 1; ----> dp[i][j] = Math.min(1 + dp[i][j - arr[i - 1]], dp[i - 1][j]);
````

[ClimbingStairs](DP/MISC/a03ClimbingStairs/ClimbingStairs.java)
````
dp[0], dp[1] = 1 ---> dp[n+1]  ----> dp[i] = dp[i-1] + dp[i-2]
````

[HouseRobber](DP/MISC/a04HouseRobber01/HouseRobber1.java)
````
dp[0] = nums[0]; dp[1] = Math.max(nums[0], nums[1]);  ----> dp[n]  ---> dp[i] = Math.max(dp[i - 1], nums[i] + dp[i - 2]);
````

[HouseRobber2](DP/MISC/a05HouseRobber02/HouseRobber2.java)
````
skipFirstHouse, skipLastHouse ---> return Math.max(lootSkippingLast, lootSkippingFirst);
````

[NumberOfIslands](Matrix/a05NumberOfIslands/NumberOfIslands.java)
````
res = res + dfs(grid, i, j);
````

[Set Matrix Zeroes](Matrix/a08SetMatrixZeroes/SetMatrixZeroes.java)
````
Input: matrix = [[1,1,1],[1,0,1],[1,1,1]] Output: [[1,0,1],[0,0,0],[1,0,1]]
if(matrix[i][j]==0){rows.add(i);columns.add(j);}
````

[WordSearch](Matrix/a06WordSearch/WordSearch.java)
````
Input: board = [["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]], word = "ABCCED" Output: true
if (board[i][j] == word.charAt(0) && dfs(board, i, j, 0, word)) {return true;} 
dfs -> count==word.length -> true ; grid[i][j]!=word.charAt(count) {return false} boolean found = dfs(i+1 || dfs(i-1 return found
````

[WordSearch2](Matrix/a06WordSearch2/WordSearch2.java)
````
Input: board = [["o","a","a","n"],["e","t","a","e"],["i","h","k","r"],["i","f","l","v"]], words = ["oath","pea","eat","rain"] Output: ["eat","oath"]
insert(word) -> Trie, private void dfs(char[][] board, int i, int j,TrieNode node, List<String> result) {
````

[ImplementTrie](Trie/a01ImplementTrie/Trie.java)
````
insert(word), search, startsWith
TrieNode -> TrieNode[] children = new TrieNode[26]; boolean isEndOfWord;    Root -> current = root; for(char ch: word.toCharArray()){index = ch - 'a'; if(current.children[index]==null)
3 times -> current.children[index] null check, initialization and setting next current
````

[AddAndSearchWordsDataDictionary](Trie/a02DesignAddAndSearchWordsDataStructure/WordDictionary.java)
````
Input
["WordDictionary","addWord","addWord","addWord","search","search","search","search"][[],["bad"],["dad"],["mad"],["pad"],["bad"],[".ad"],["b.."]]Output[null,null,null,null,false,true,true,true]
dfs(word, index, TrieNode current) -> If(index == word.length) return true; 
char ch = word.charAt(index); if(ch=='.') {  } int childIndex = ch-'a'; if(current.children[childIndex]==null) return false return dfs(word, index+1, current.children[childIndex])
````

[ContainsDuplicate](Array/Hashing/a09ContainsDuplicate/ContainsDuplicate.java)
````
[1,2,3,1] -> true
if (!set.add(num)) {return true}
````

[TopKFrequentElements](Heap/a05topKFrequentNumbers/TopKFrequentNumbers.java)

[ValidateBinarySearchTree](BinaryTree/a09ValidateBinarySearchTree/ValidateBinarySearchTree.java)
````
Input: root = [2,1,3] Output: true
isValidBST(root, Long.MIN_VALUE, Long.MAX_VALUE) -> isValidBST(TreeNode root, long min, long max)   return isBST(root.left, min, root.val) && isBST(root.right, root.val, max);
````

[InvertBinaryTree](BinaryTree/a17InvertBinaryTree/InvertBinaryTree.java)
````
TreeNode left = invertTree(root.left);TreeNode right = invertTree(root.right);root.left = right;root.right = left;
````

[KthSmallestElementInBST](BinaryTree/a11KthSmallestElementInBST/KthSmallestElementInBST.java)
````
Input: root = [5,3,6,2,4,null,null,1], k = 3 Output: 3
int left = kthSmallest(root.left, k);if (left != -1) {return left};count++; if (count == k){return root.val}; return kthSmallest(root.right, k)
````

[BinaryTreeLevelOrderTraversal](BinaryTree/a05BinaryTreeLevelOrderTraversal/BinaryTreeLevelOrderTraversal.java)
````
Queue<TreeNode> queue = new LinkedList<>(); while(!queue.isEmpty()){int size = queue.size(); List<Integer> level; for(int i=0; i<size; i++) { TreeNode node = queue.poll();
````

[MaximumDepthOfBinaryTree](BinaryTree/a02MaximumDepthOfBinaryTree/MaximumDepthOfBinaryTree.java)
````
if(root==null) return 0; int left = maxDepth(root.left); int right = maxDepth(root.right); return 1 + Math.max(root.left, root.right);
````

[BinaryTreeFromPreOrderAndInOrder](BinaryTree/a22BinaryTreeFromPreorderInorder/BinaryTreeFromPreorderInorder.java)
````
root = new TreeNode(preOrder[preStart]);
root.left = helper(prestart+1, instart, inIndex-1, preOrder, inOrder);
root.right = helper(prestart+ inIndex - inStart + 1, index+1, inEnd, preOrder, inOrder);
````

[LCAofBST](BinaryTree/a12LCAofBST/LCAofBST.java)
````
if (p.val < root.val && q.val < root.val) return lowestCommonAncestor(root.left, p, q);
if (p.val > root.val && q.val > root.val) return lowestCommonAncestor(root.right, p, q);
````

[BinaryTreeMaxPathSum](BinaryTree/a25BinaryTreeMaxPathSum/BinaryTreeMaxPathSum.java)
````
Input: root = [1,2,3] Output: 6
 int left = Math.max(helper(root.left), 0);int right = Math.max(helper(root.right), 0);max = Math.max(max, root.val + left + right);return root.val + Math.max(left, right);
````