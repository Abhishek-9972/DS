package DS.Array.a02ContainerWithMostWater;

/**
 * https://leetcode.com/problems/container-with-most-water/description/
 *
 * Input: height = [1,8,6,2,5,4,8,3,7]
 * Output: 49
 * Explanation: The above vertical lines are represented by array [1,8,6,2,5,4,8,3,7]. In this case, the max area of water (blue section) the container can contain is 49.
 *
 */
public class ContainerWithMostWater {
    public int maxArea(int[] height) {
        int aPointer = 0;
        int bPointer = height.length - 1;
        int max = 0;

        while (aPointer < bPointer) {
            if (height[aPointer] < height[bPointer]) {
                max = Math.max(max, height[aPointer] * (bPointer - aPointer));
                aPointer++;
            } else {
                max = Math.max(max, height[bPointer] * (bPointer - aPointer));
                bPointer--;
            }
        }
        return max;
    }
}
