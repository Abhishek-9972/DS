package ds.Array.a21MajorityElement1;

public class MajorityElement1 {
    //Moore voting algorithm

    public int majorityElement(int[] nums) {
        int ele = nums[0];
        int count = 0;

        for(int num: nums){
            if(count==0){
                ele = num;
            }
            if(ele == num){
                count++;
            }
            else{
                count--;
            }
        }
        return ele;
    }
}

/**
 *
 * If same num -> Update the count
 * If count is zero -> Make ele as num
 * Else Count--
 *
 *
 */