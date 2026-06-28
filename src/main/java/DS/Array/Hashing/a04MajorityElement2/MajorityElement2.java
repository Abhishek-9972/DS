package DS.Array.Hashing.a04MajorityElement2;

import java.util.ArrayList;
import java.util.List;

public class MajorityElement2 {

    public List<Integer> majorityElement(int[] nums) {

        int majority1 = 0, majority2 = 0;
        int votes1 = 0, votes2 = 0;

        // First Pass - Find Potential Majority Elements
        for (int num : nums) {

            if (majority1 == num) {
                votes1++;
            } else if (majority2 == num) {
                votes2++;
            } else if (votes1 == 0) {
                majority1 = num;
                votes1 = 1;
            } else if (votes2 == 0) {
                majority2 = num;
                votes2 = 1;
            } else {
                votes1--;
                votes2--;
            }
        }

        // Second Pass - Verify Candidates
        votes1 = 0;
        votes2 = 0;

        for (int num : nums) {
            if (num == majority1) {
                votes1++;
            } else if (num == majority2) {
                votes2++;
            }
        }

        List<Integer> result = new ArrayList<>();

        if (votes1 > nums.length / 3) {
            result.add(majority1);
        }

        if (votes2 > nums.length / 3) {
            result.add(majority2);
        }

        return result;
    }
}



