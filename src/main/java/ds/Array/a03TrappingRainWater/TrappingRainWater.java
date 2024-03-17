package ds.Array.a03TrappingRainWater;

/**
 *
 * https://leetcode.com/problems/trapping-rain-water/description/
 *
 * Input: height = [0,1,0,2,1,0,1,3,2,1,2,1]
 * Output: 6
 * Explanation: The above elevation map (black section) is represented by array [0,1,0,2,1,0,1,3,2,1,2,1]. In this case, 6 units of rain water (blue section) are being trapped.
 */
public class TrappingRainWater {
    public int trap(int[] height) {
        int n = height.length;
        int[] maxLeft = new int[n];
        maxLeft[0] = height[0];

        for (int i = 1; i < n; i++) {
            maxLeft[i] = Math.max(maxLeft[i - 1], height[i]);
        }

        int[] maxRight = new int[n];
        maxRight[n - 1] = height[n - 1];

        for (int i = n - 2; i >= 0; i--) {
            maxRight[i] = Math.max(maxRight[i + 1], height[i]);
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = Math.min(maxLeft[i], maxRight[i]) - height[i];
        }

        int res = 0;
        for (int i = 0; i < n; i++) {
            res = res + result[i];
        }

        return res;
    }
}

