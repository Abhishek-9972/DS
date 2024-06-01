package DS.BinarySearch.a07findPeakElement;

public class PeakElement {

    public int binarySearch(int[] arr) {
        int start = 0;
        int end = arr.length - 1;
        int n = arr.length - 1;
        while (start <= end) {
            int mid = (start + end) / 2;
            if (arr[mid] >= arr[(mid + 1) % n] && arr[mid] >= arr[(mid + n - 1) % n]) {
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
        int [] arr = {5, 10, 20, 15};
        PeakElement peakElement = new PeakElement();
        int i = peakElement.binarySearch(arr);
        System.out.println(arr[i]);
    }

}
