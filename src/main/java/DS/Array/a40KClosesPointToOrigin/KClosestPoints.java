package DS.Array.a40KClosesPointToOrigin;

import java.util.PriorityQueue;


class KClosestPoints {
    public int[][] kClosest(int[][] points, int k) {
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> (b[0]*b[0] + b[1]*b[1] -
                (a[0]*a[0] + a[1]*a[1])));
        for(int [] point : points){
            pq.add(point);
            if(pq.size()>k){
                pq.poll();
            }
        }

        int [][] result = new int[k][2];
        for(int i=0; i<k; i++){
            result[i] = pq.poll();
        }

        return result;

    }
}

/**

 I had the same confusion and I think I figured it out. Since we're finding the closest points to origin the formula can be simplified.

 1.) sqrt( (x1 - x2)^2 + (y1 - y2)^2 ) <== Euclidean distance formula given in the problem statement.
 2.) sqrt( (x1 - 0)^2 + (y1 - 0)^2 ) <== Simplified distance to (0, 0)
 3.) sqrt( (x1)^2 + (y1)^2 ) <== Zero's do nothing
 4.) (x1)^2 + (y1)^2 <== We don't really need to calculate the square root
 5.) x1*x1 + y1*y1 <== This is easier to code than Math.pow(). This is our distance to origin.
 6.) The Priority Queue takes a comparator to compare two distances to origin (one, two)
 7.) (one, two) -> (two's distance to origin) - (one's distance to origin)
 8.) (one, two) -> (two[x]*two[x] + two[y]*two[y]) - (one[x]*one[x] + one[y]*one[y])




 */