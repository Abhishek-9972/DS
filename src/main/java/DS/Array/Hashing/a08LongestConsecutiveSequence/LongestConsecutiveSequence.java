package DS.Array.Hashing.a08LongestConsecutiveSequence;

import java.util.HashSet;
import java.util.Set;

/**
 * Time Complexity: O(n)
 * Space Complexity: O(n)
 *
 * https://www.youtube.com/watch?v=P6RZZMu_maU
 */
public class LongestConsecutiveSequence {

    public int longestConsecutive(int[] nums) {

        Set<Integer> set = new HashSet<>();

        for (int num : nums) {
            set.add(num);
        }

        int longest = 0;

        for (int num : set) {

            //Checking this to know "Is there a number immediately before me?"
            if (!set.contains(num - 1)) {

                int currentNum = num;
                int currentStreak = 1;

                while (set.contains(currentNum + 1)) {
                    currentNum++;
                    currentStreak++;
                }

                longest = Math.max(longest, currentStreak);
            }
        }

        return longest;
    }
}


/**
 * Input: nums = [100,4,200,1,3,2]
 * Output: 4
 * Explanation: The longest consecutive elements sequence is [1, 2, 3, 4]. Therefore its length is 4.
 */