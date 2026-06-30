package DS.Intervals.a05MeetingRooms2;

import java.util.Arrays;
import java.util.PriorityQueue;

public class MeetingRooms2 {

    public int minMeetingRooms(int[][] intervals) {

        if (intervals.length == 0) {
            return 0;
        }

        // Sort by start time
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        // Min Heap stores meeting end times
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        // First meeting occupies one room
        minHeap.offer(intervals[0][1]);

        // Process remaining meetings
        for (int i = 1; i < intervals.length; i++) {

            int currentStart = intervals[i][0];
            int currentEnd = intervals[i][1];

            // Room becomes free
            if (currentStart >= minHeap.peek()) {
                minHeap.poll();
            }

            // Allocate room to current meeting
            minHeap.offer(currentEnd);
        }

        return minHeap.size();
    }

    public static void main(String[] args) {

        MeetingRooms2 obj = new MeetingRooms2();

        int[][] intervals = {
                {0, 30},
                {5, 10},
                {15, 20}
        };

        System.out.println(obj.minMeetingRooms(intervals)); // 2
    }
}