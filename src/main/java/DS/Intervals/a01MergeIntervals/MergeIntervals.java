package DS.Intervals.a01MergeIntervals;

import java.util.Arrays;
import java.util.Stack;

public class MergeIntervals {
    public int[][] merge(int[][] intervals) {
        if(intervals.length==0){
            return new int[][]{};
        }

        //Sort the intervals
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
        Stack<int[]> stack = new Stack<>();
        stack.push(intervals[0]);

        //Iterate and merge
        for (int i = 1; i < intervals.length; i++) {
            int[] currentInterval = intervals[i];
            int[] topInterval = stack.peek();

            if (currentInterval[0] <= topInterval[1]) {
                topInterval[1] = Math.max(topInterval[1], currentInterval[1]);
            } else {
                stack.push(currentInterval);
            }
        }

        //Construct the result
        int[][] mergedIntervals = new int[stack.size()][2];
        for (int i = stack.size() - 1; i >= 0; i--) {
            mergedIntervals[i] = stack.pop();
        }

        return mergedIntervals;

    }
}