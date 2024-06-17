package DS.BinarySearch.a13SquareRootOfANumber;

public class SquareRoot {
    public int mySqrt(int x) {
        if (x == 0) {
            return 0;
        }
        int first = 1, last = x;
        while (first <= last) {
            int mid = first + (last - first) / 2;
            if (mid == x / mid) {
                return mid;
            } else if (mid > x / mid) {
                last = mid - 1;
            } else {
                first = mid + 1;
            }
        }
        return last;
    }
}