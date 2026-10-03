package DS.Array.Hashing.a10FindDuplicate;

/**
 * https://www.youtube.com/watch?v=_n5MR8IxR6c
 *
 * https://leetcode.com/problems/find-the-duplicate-number/description/
 *
 */
public class FindDuplicate {
    public int findDuplicate(int[] nums) {

        int slow = nums[0];
        int fast = nums[0];

        while (true) {
            slow = nums[slow];
            fast = nums[nums[fast]];

            if (slow == fast) {
                break;
            }
        }

        slow = nums[0];

        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }

        return slow;
    }
}
