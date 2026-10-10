package DS.String.a08TaskScheduler;

import java.util.Arrays;

/**
 * https://leetcode.com/problems/task-scheduler/description/
 * https://www.youtube.com/watch?v=eGf-26OTI-A
 */
public class TaskScheduler {
    public int leastInterval(char[] tasks, int n) {
        int[] charMap = new int[26];
        for (char c : tasks) {
            charMap[c - 'A']++;
        }

        Arrays.sort(charMap);

        // The most frequent task creates (max frequency - 1) gaps, each needing n cooldown slots.
        int maxVal = charMap[25] - 1;
        int idleSlots = maxVal * n;

        for (int i = 24; i >= 0; i--) {
            idleSlots = idleSlots - Math.min(charMap[i], maxVal);
        }

        // If idle slots is zero that means no extra slot required
        return idleSlots > 0 ? idleSlots + tasks.length : tasks.length;
    }
}