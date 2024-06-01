package DS.Array.a23RemoveDuplicatesFromSortedArray02;

public class RemoveDuplicatesFromSortedArray02 {
    public int removeDuplicates(int[] nums) {
        int counter = 0;
        for (int i = 0; i < nums.length; i++) {
            while (i + 2 < nums.length && nums[i] == nums[i + 2]) {
                i++;
            }
            nums[counter] = nums[i];
            counter++;
        }
        return counter;
    }
}