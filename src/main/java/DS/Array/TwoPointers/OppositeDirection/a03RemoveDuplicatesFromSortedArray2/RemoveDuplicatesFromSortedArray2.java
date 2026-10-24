package DS.Array.TwoPointers.OppositeDirection.a03RemoveDuplicatesFromSortedArray2;

public class RemoveDuplicatesFromSortedArray2 {

    public int removeDuplicates(int[] nums) {
        int k = 0;

        for (int num : nums) {
            if (k < 2 || num != nums[k - 2]) {
                nums[k++] = num;
            }
        }

        return k;
    }
}