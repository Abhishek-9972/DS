package ds.sorting.bubbleSort;

public class BubbleSort {

    private static void bubbleSort(int[] arr) {
        int n = arr.length;
        int temp;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < n - i; j++) {
                if (arr[j] < arr[j - 1]) {
                    temp = arr[j - 1];
                    arr[j - 1] = arr[j];
                    arr[j] = temp;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = {11, 5, 6, 2, 1, 8};
        bubbleSort(arr);
        for (int d : arr) {
            System.out.println(d);
        }
    }
}
