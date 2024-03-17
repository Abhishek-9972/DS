package ds.Array.a16SingleNumber2;

import java.util.HashMap;
import java.util.Map;

/**
 * https://leetcode.com/problems/single-number-ii/description/
 */
public class SingleNumber2 {
    public int singleNumber(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(nums[i])) {
                int value = map.get(nums[i]);
                map.put(nums[i], value + 1);
            } else {
                map.put(nums[i], 1);
            }
        }

        for (Map.Entry<Integer, Integer> m1 : map.entrySet()) {
            if (m1.getValue() == 1) {
                return m1.getKey();
            }
        }
        return -1;
    }
}

