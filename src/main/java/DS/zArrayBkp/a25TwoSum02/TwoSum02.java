package DS.zArrayBkp.a25TwoSum02;

/**
 * In regular Two Sum, the array is unsorted, so we use a HashMap.
 * In Two Sum II, the array is already sorted.
 * That means we can use:
 * Two Pointers
 */
public class TwoSum02 {
    public int[] twoSum(int[] numbers, int target) {

        int left = 0;
        int right = numbers.length - 1;

        while (left < right) {

            int sum = numbers[left] + numbers[right];

            if (sum == target) {
                return new int[]{left + 1, right + 1};
            }

            if (sum < target) {
                left++;
            } else {
                right--;
            }
        }

        return new int[]{-1, -1};
    }
}