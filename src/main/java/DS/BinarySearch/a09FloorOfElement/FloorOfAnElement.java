package DS.BinarySearch.a09FloorOfElement;

/**
 * Array = [1,3,4,6,7,9]
 *
 * Element = 5
 *
 * Floor = 4
 */
public class FloorOfAnElement {

    public int floorOfAnElement(int[] arr, int ele) {

        int start = 0;
        int end = arr.length - 1;
        int result = -1;

        while (start <= end) {

            int mid = start + (end - start) / 2;

            if (arr[mid] == ele)
                return arr[mid];
            else if (arr[mid] < ele) {
                result = arr[mid];
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 4, 6, 7, 9};

        FloorOfAnElement obj = new FloorOfAnElement();

        System.out.println(obj.floorOfAnElement(arr, 5));
    }
}