package DS.Array.a36EquibiliriumPoint;

import java.util.Arrays;
import java.util.List;

public class EquilibriumPoint {

    public int pivotIndex(List<Integer> nums) {
        int left = 0, pivot = 0, right = 0;
        for (int i = 1; i < nums.size(); i++) {
            right += nums.get(i);
        }
        while (pivot < nums.size() - 1 && right != left) {
            pivot++;
            right -= nums.get(pivot);
            left += nums.get(pivot - 1);
        }
        return (left == right) ? pivot : -1;
    }

    public static void main(String[] args) {
        List<Integer> nums = Arrays.asList(1, 7, 3, 6, 5, 6);
        int result = new EquilibriumPoint().pivotIndex(nums);
        System.out.println(result);
    }
}
