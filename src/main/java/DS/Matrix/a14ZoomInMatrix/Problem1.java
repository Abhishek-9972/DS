package DS.Matrix.a14ZoomInMatrix;

public class Problem1 {

    public int[][] fetchMatrix(int[][] arr, int mul){
        int rowLen = arr.length * mul;
        int colLen = arr[0].length * mul;
        int[][] matrix = new int[rowLen][colLen];
        for(int i=0; i<mul*arr.length; i++){
            for(int j=0; j<mul*arr[0].length; j++){
                matrix[i][j] = arr[i/mul][j/mul];
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        Problem1 problem1 = new Problem1();
        int[][] arr = {{1,0,1},{1,0,1}};
        int mul = 3;
        int [][] matrix = problem1.fetchMatrix(arr, mul);
    }

}
