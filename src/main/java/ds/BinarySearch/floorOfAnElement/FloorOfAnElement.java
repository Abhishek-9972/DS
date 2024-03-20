package ds.BinarySearch.floorOfAnElement;

public class FloorOfAnElement {
    private int floorOfAnElement(int[] arr, int ele) {
        int start = 0;
        int result = -1;
        int end = arr.length - 1;
        while (start <= end) {
            int mid = start + (end - start) / 2;
            if (arr[mid] < ele) {
                start = mid + 1;
            } else if (arr[mid] > ele) {
                result = arr[mid];
                end = mid - 1;
            } else {
                return arr[mid];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int [] arr = {1,3,4,6,7,9};
        FloorOfAnElement floorOfAnElement = new FloorOfAnElement();
        int i = floorOfAnElement.floorOfAnElement(arr, 5);
        System.out.println(i);
    }
}