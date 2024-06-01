package DS.BinarySearch.a09FloorOfElement;

public class FloorOfElement {
    private int floorOfElement(int[] arr, int ele) {
        int start = 0;
        int result = -1;
        int end = arr.length - 1;
        while (start <= end) {
            int mid = start + (end - start) / 2;
            if (arr[mid] < ele) {
                result = arr[mid];
                start = mid + 1;
            } else if (arr[mid] > ele) {
                end = mid - 1;
            } else {
                return arr[mid];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int [] arr = {1,3,4,6,7,9};
        FloorOfElement ceilOfElement = new FloorOfElement();
        int i = ceilOfElement.floorOfElement(arr, 5);
        System.out.println(i);
    }
}