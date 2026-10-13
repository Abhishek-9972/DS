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