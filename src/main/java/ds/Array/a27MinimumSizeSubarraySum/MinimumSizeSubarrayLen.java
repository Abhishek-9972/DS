package ds.Array.a27MinimumSizeSubarraySum;

class MinimumSizeSubarrayLen {
    public int minSubArrayLen(int target, int[] nums) {
        int result = Integer.MAX_VALUE;
        int left = 0;
        int valSum = 0;

        for(int i =0; i<nums.length; i++){
            valSum = valSum + nums[i];

            while(valSum>=target){
                result = Math.min(result, i+1-left);
                valSum = valSum - nums[left];
                left++;
            }
        }

        return result!=Integer.MAX_VALUE ? result : 0;
    }
}