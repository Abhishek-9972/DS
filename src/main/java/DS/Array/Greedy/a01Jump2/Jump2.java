package DS.Array.Greedy.a01Jump2;

public class Jump2 {

    public int jump(int[] nums) {

        int jumps = 0;
        int currentEnd = 0;
        int reachable = 0;

        for (int i = 0; i < nums.length - 1; i++) {

            reachable = Math.max(reachable, i + nums[i]);

            if (i == currentEnd) {
                jumps++;
                currentEnd = reachable;
            }
        }

        return jumps;
    }
}