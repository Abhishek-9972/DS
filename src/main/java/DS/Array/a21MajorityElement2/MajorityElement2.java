package DS.Array.a21MajorityElement2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://leetcode.com/problems/majority-element-ii/description/
 */
public class MajorityElement2 {
    public List<Integer> majorityElement(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i : nums) {
            if (map.containsKey(i)) {
                map.put(i, map.get(i) + 1);
            } else {
                map.put(i, 1);
            }
        }
        List<Integer> list = new ArrayList<>();
        int maxValue = nums.length / 3;
        for (Map.Entry<Integer, Integer> m : map.entrySet()) {
            if (m.getValue() > maxValue) {
                list.add(m.getKey());
            }
        }
        return list;
    }
}

