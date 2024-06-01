package DS.DP.MISC.a01Jump2;

public class Jump2 {
    public int jump(int[] nums) {
        int end = 0, reachable = 0;
        int jump=0;

        for(int i=0; i<nums.length-1; i++){
            reachable = Math.max(reachable, i+nums[i]);

            if(i==end){
                jump++;
                end = reachable;
            }
        }

        return jump;
    }

    public static void main(String[] args) {
        int [] nums = {2,3,1,1,4};
        Jump2 jump2 = new Jump2();
        System.out.println(jump2.jump(nums));
    }
}