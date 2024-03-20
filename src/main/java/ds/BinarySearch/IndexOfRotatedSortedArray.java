package ds.BinarySearch;

public class IndexOfRotatedSortedArray {

    public int binarySearch(int[] arr) {
        int start = 0;
        int end = arr.length - 1;
        int n = arr.length - 1;
        while (start <= end) {
            int mid = (start + end) / 2;
            if (arr[mid] <= arr[(mid + 1) % n] && arr[mid] <= arr[(mid + n - 1) % n]) {
                return mid;
            }
            if (arr[start] < arr[mid]) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        int[] arr = {18, 22, 2, 4, 6, 8, 10, 12};
        IndexOfRotatedSortedArray indexOfRotatedSortedArray = new IndexOfRotatedSortedArray();
        System.out.println(indexOfRotatedSortedArray.binarySearch(arr));
    }
}
