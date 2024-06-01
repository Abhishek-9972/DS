package DS.Intervals.InsertIntervals;

import java.util.Arrays;
import java.util.Stack;

public class InsertIntervals {
    public int[][] insert(int[][] intervals, int[] newInterval) {

        if(intervals.length==0){
            return new int[][]{newInterval};
        }

        int[][] modifiedIntervals = new int[intervals.length+1][intervals[0].length+1];

        for(int i=0; i<intervals.length; i++){
            modifiedIntervals[i] = intervals[i];
        }

        modifiedIntervals[intervals.length] = newInterval;

        //Sort the intervals
        Arrays.sort(modifiedIntervals, (a, b) -> Integer.compare(a[0], b[0]));
        Stack<int[]> stack = new Stack<>();
        stack.push(modifiedIntervals[0]);

        //Iterate and merge
        for (int i = 1; i < modifiedIntervals.length; i++) {
            int[] currentInterval = modifiedIntervals[i];
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

    public static void main(String[] args) {
        int[][] a = new int[1][2];
        a[0] = new int[]{1,5};
        int[] b = new int[]{0,3};
        InsertIntervals intervals = new InsertIntervals();
        intervals.insert(a, b);
    }
}