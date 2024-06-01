package DS.Array.a28ProductOfArrayExceptSelf;

public class ProductOfArrayExceptSelf {
    public int[] productExceptSelf(int[] nums) {

        int[] res = new int[nums.length];

        int mul = 1;
        boolean zeroVal = false;
        for (int i : nums) {
            if (i == 0) {
                if(zeroVal){
                    return res;
                }
                i = 1;
                zeroVal = true;
            }
            mul = mul * i;
        }

        System.out.println(mul);

        for (int i = 0; i < nums.length; i++) {
            if(zeroVal){
                if(nums[i]==0){
                    res[i] = mul;
                }
            }

            else{
                res[i] = mul / nums[i];
            }

        }

        return res;
    }
}