package DS.Intervals.a06NonOverlappingIntervals;

import java.util.Arrays;

/**
 * https://leetcode.com/problems/non-overlapping-intervals/description/
 */
public class NonOverlappingIntervals {

    public int eraseOverlapIntervals(int[][] intervals) {

        if (intervals.length == 0) {
            return 0;
        }

        // Sort intervals by end time
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));

        int removed = 0;
        int previousEnd = intervals[0][1];

        for (int i = 1; i < intervals.length; i++) {

            // Overlapping interval
            if (intervals[i][0] < previousEnd) {
                removed++;
            } else {
                // Non-overlapping interval
                previousEnd = intervals[i][1];
            }
        }

        return removed;
    }
}