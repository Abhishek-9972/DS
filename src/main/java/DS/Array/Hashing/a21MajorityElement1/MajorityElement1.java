package DS.Array.Hashing.a21MajorityElement1;

public class MajorityElement1 {

    public int majorityElement(int[] nums) {

        int majority = 0;
        int votes = 0;

        for (int num : nums) {

            if (majority == num) {
                votes++;
            } else if (votes == 0) {
                majority = num;
                votes = 1;
            } else {
                votes--;
            }
        }

        return majority;
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