package DS.Array.a13Jump1;

/**
 * https://leetcode.com/problems/jump-game/description/
 */
public class Jump1 {
    public boolean canJump(int[] nums) {
        int reachable = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i > reachable) return false;
            reachable = Math.max(reachable, i + nums[i]);
        }
        return true;
    }
}

