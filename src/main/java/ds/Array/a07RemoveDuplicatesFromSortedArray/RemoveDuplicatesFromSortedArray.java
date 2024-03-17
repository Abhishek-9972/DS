package ds.Array.a07RemoveDuplicatesFromSortedArray;

public class RemoveDuplicatesFromSortedArray {
    public int removeDuplicates(int[] nums) {
        int counter = 0;
        for (int i = 0; i < nums.length; i++) {
            while (i + 1 < nums.length && nums[i] == nums[i + 1]) {
                i++;
            }
            nums[counter] = nums[i];
            counter++;
        }
        return counter;
    }
}

