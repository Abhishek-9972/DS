package DS.BinarySearch.a08CeilOfAnElement;

/**
 * Array = [1,3,4,6,7,9]
 * <p>
 * Element = 5
 * <p>
 * Ceil = 6
 *
 *
 */
public class CeilOfAnElement {

    public int ceilOfAnElement(int[] arr, int ele) {

        int start = 0;
        int end = arr.length - 1;
        int result = -1;

        while (start <= end) {

            int mid = start + (end - start) / 2;

            if (arr[mid] == ele)
                return arr[mid];
            else if (ele > arr[mid]) {
                start = mid + 1;
            } else {
                //“I found a value ≥ target. Can I find a smaller valid one?”
                result = arr[mid];
                end = mid - 1;
            }
        }

        return result;
    }
}