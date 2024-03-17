package ds.Matrix.a02SprialMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * https://leetcode.com/problems/spiral-matrix/description/
 */
public class SpiralMatrix {
    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> res = new ArrayList<>();
        int rs = 0;
        int re = matrix.length - 1;
        int cs = 0;
        int ce = matrix[0].length - 1;

        while (rs <= re && cs <= ce) {
            for (int i = cs; i <= ce; i++) {
                res.add(matrix[rs][i]);
            }
            rs++;
            for (int i = rs; i <= re; i++) {
                res.add(matrix[i][ce]);
            }
            ce--;
            if (rs <= re) {
                for (int i = ce; i >= cs; i--) {
                    res.add(matrix[re][i]);
                }
                re--;
            }
            if (cs <= ce) {
                for (int i = re; i >= rs; i--) {
                    res.add(matrix[i][cs]);
                }
                cs++;
            }
        }
        return res;
    }
}
