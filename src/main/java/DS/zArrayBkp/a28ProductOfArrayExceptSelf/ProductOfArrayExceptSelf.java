package DS.zArrayBkp.a28ProductOfArrayExceptSelf;

/**
 * O(n) time and O(1) extra space.
 *
 * https://leetcode.com/problems/product-of-array-except-self/description/
 */
public class ProductOfArrayExceptSelf {
    public int[] productExceptSelf(int[] nums) {

        int n = nums.length;
        int[] result = new int[n];
        result[0] = 1;

        for (int i = 1; i < n; i++) {
            result[i] = result[i - 1] * nums[i - 1];
        }

        int rightProduct = 1;

        for (int i = n - 1; i >= 0; i--) {

            result[i] *= rightProduct;

            rightProduct *= nums[i];
        }

        return result;
    }
}