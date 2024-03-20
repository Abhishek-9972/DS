package ds.BinarySearch;

public class FindElementInRotatedSortedArray {
    public static void main(String[] args) {
        int[] arr = {18, 22, 2, 4, 6, 8, 10, 12};
        int ele = 8;

        // Index of smallest element
        FindElementInRotatedSortedArray findElementInRotatedSortedArray = new FindElementInRotatedSortedArray();
        int index = findElementInRotatedSortedArray.binarySearchFindMin(arr, 0, arr.length-1);

        int x = findElementInRotatedSortedArray.binarySearch(arr, ele,0 , index -1);
        int y = findElementInRotatedSortedArray.binarySearch(arr, ele, index, arr.length-1);

        if(x == -1 && y == -1)
        {
            System.out.println("Element not present");
        }
        else
        {
            if(x != -1)
            {
                System.out.println("Index of element : "+ x);
            }
            else
            {
                System.out.println("Index of element : "+ y);
            }
        }


    }


    public int binarySearch(int[] arr, int key, int start , int end) {

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
        return -1;
    }

    public int binarySearchFindMin(int[] arr, int start, int end) {
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
}
