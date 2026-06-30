package DS.Intervals.a04MeetingRooms1;

import java.util.Arrays;

public class MeetingRooms1 {

    public boolean canAttendMeetings(int[][] intervals) {

        if (intervals.length <= 1) {
            return true;
        }

        // Sort by start time
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        // Check for overlap
        for (int i = 1; i < intervals.length; i++) {

            if (intervals[i][0] < intervals[i - 1][1]) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {

        MeetingRooms1 obj = new MeetingRooms1();

        int[][] intervals = {
                {0, 30},
                {5, 10},
                {15, 20}
        };

        System.out.println(obj.canAttendMeetings(intervals)); // false
    }
}