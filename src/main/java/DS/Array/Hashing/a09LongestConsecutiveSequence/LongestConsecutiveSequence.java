package DS.Array.Hashing.a09LongestConsecutiveSequence;

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
