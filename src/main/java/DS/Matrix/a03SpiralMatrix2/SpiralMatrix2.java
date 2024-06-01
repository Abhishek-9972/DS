package DS.Matrix.a03SpiralMatrix2;

/**
 * https://leetcode.com/problems/spiral-matrix-ii/description/
 */
public class SpiralMatrix2 {
    public int[][] generateMatrix(int n) {
        int[][] res = new int[n][n];

        int rs = 0;
        int re = n - 1;
        int cs = 0;
        int ce = n - 1;
        int counter = 1;

        while (rs <= re && cs <= ce) {
            for (int i = cs; i <= ce; i++) {
                res[rs][i] = counter++;
            }
            rs++;
            for (int i = rs; i <= re; i++) {
                res[i][ce] = counter++;
                ;
            }
            ce--;
            if (rs <= re) {
                for (int i = ce; i >= cs; i--) {
                    res[re][i] = counter++;
                }
                re--;
            }
            if (cs <= ce) {
                for (int i = re; i >= rs; i--) {
                    res[i][cs] = counter++;
                }
                cs++;
            }
        }
        return res;

    }
}

