package ds.Matrix.a10ZeroOneMatrix;

import java.util.LinkedList;
import java.util.Queue;

class ZeroOneMatrix {

    public int[][] updateMatrix(int[][] arr) {
        int [][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        Queue<int[]> q = new LinkedList<>();

        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr[0].length;j++){
                if(arr[i][j]==0){
                    q.add(new int[]{i,j});
                }else{
                    arr[i][j] = Integer.MAX_VALUE;
                }
            }
        }

        while(!q.isEmpty()){
            int[] cell = q.remove();
            for(int[]dir:directions){
                int xInd =cell[0] + dir[0];
                int yInd =cell[1] + dir[1];
                if(xInd>=0&&xInd<arr.length&&yInd>=0&&yInd<arr[0].length&&arr[cell[0]][cell[1]]+1<arr[xInd][yInd]){
                    q.add(new int[]{xInd,yInd});
                    arr[xInd][yInd] =arr[cell[0]][cell[1]]+1;
                }
            }
        }
        return arr;
    }
}