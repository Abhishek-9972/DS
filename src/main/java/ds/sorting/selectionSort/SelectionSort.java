package ds.sorting.selectionSort;

public class SelectionSort {
    public static void main(String[] args) {
        int[] arr = {5, 6, 2, 4, 1, 11, 7, 3};
        int pos;
        for (int i = 0; i < arr.length; i++) {
            pos = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] < arr[pos]) {
                    pos = j;
                }
            }
            int temp = arr[i];
            arr[i] = arr[pos];
            arr[pos] = temp;

        }

        for (int d : arr) {
            System.out.print(d);
        }
    }
}
