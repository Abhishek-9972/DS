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