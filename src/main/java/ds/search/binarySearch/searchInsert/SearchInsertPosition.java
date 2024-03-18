package ds.search.binarySearch.searchInsert;

public class SearchInsertPosition {

    public int binarySearch(int[] arr, int key) {
        int start = 0;
        int end = arr.length - 1;
        while (start <= end) {
            int mid = (start + end) / 2;
            if (key == arr[mid]) {
                return mid;
            }
            if (key < arr[mid]) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }
        return end+1;
    }

    public static void main(String[] args) {
        SearchInsertPosition searchInsertPosition = new SearchInsertPosition();
        int i = searchInsertPosition.binarySearch(new int[]{1, 3, 5, 6}, 1);
        System.out.println(i);
    }
}
