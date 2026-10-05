package DS.BinarySearch.a10minimumDifferenceElement;

public class MinimumDiffElement {
    private int minimumDiffElement(int[] arr, int ele) {
        int start = 0;
        int end = arr.length - 1;
        while (start <= end) {
            int mid = start + (end - start) / 2;
            if (ele == arr[mid]) {
                return arr[mid];
            } else if (ele > arr[mid]) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        if (Math.abs(arr[start] - ele) < Math.abs(arr[end]) - ele) {
            return arr[start];
        } else {
            return arr[end];
        }
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 4, 7, 9};
        MinimumDiffElement minimumDiffElement = new MinimumDiffElement();
        int i = minimumDiffElement.minimumDiffElement(arr, 5);
        System.out.println(i);
    }

}
