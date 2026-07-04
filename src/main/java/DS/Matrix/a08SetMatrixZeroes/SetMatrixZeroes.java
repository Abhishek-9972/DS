package DS.Matrix.a08SetMatrixZeroes;

import java.util.ArrayList;
import java.util.List;

public class SetMatrixZeroes {
    public void setZeroes(int[][] matrix) {

        List<Integer> rows = new ArrayList<>();
        List<Integer> columns = new ArrayList<>();


        for(int i=0; i<matrix.length; i++){
            for(int j=0; j<matrix[0].length; j++){
                if(matrix[i][j]==0){
                    rows.add(i);
                    columns.add(j);
                }
            }
        }

        for(Integer row: rows){
            for(int i=0; i<matrix[0].length; i++){
                matrix[row][i] = 0;
            }
        }

        for(Integer column: columns){
            for(int i=0; i<matrix.length; i++){
                matrix[i][column] = 0;
            }
        }
    }
}