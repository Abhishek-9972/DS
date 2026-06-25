package DS.Array.a00ArraysPatternWise.Hashing.a11FindDuplicate;

/**
 * https://www.youtube.com/watch?v=_n5MR8IxR6c
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
