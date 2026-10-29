package DS.Array.TwoPointers.OppositeDirection.a03RemoveDuplicatesFromSortedArray2;

public class RemoveDuplicatesFromSortedArray2 {

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