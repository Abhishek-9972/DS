package DS.Array.TwoPointers.a05MoveZeroes;

public class MoveZeroes {
    int counter = 0;
    public void moveZeroes(int[] nums) {
        for(int num: nums){
            if(num!=0){
                nums[counter++]=num;
            }
        }
        while(counter<nums.length){
            nums[counter++]=0;
        }
    }
}