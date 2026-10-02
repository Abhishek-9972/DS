package DS.Array.TwoPointers.sameDirection.a03BoatsToSavePeople;

import java.util.Arrays;

/**
 * https://leetcode.com/problems/boats-to-save-people/
 * Time Complexity - O(n log n)
 * Space Complexity - O(1)
 */
public class BoatsToSavePeople {
    public int numRescueBoats(int[] people, int limit) {
        // Sort to enable greedy pairing of lightest with heaviest
        Arrays.sort(people);
        int light = 0, heavy = people.length - 1;
        int boats = 0;

        while (light <= heavy) {
            // Check if lightest and heaviest can share a boat
            if (people[light] + people[heavy] <= limit) {
                light++;
            }
            // Heaviest always gets on this boat
            heavy--;
            boats++;
        }
        return boats;
    }
}
