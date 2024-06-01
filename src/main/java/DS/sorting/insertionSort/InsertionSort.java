package DS.sorting.insertionSort;

public class InsertionSort {
    public static int[] doInsertionSort(int[] arr) {
        int temp = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i; j > 0; j--) {
                if (arr[j] < arr[j - 1]) {
                    temp = arr[j];
                    arr[j] = arr[j - 1];
                    arr[j - 1] = temp;
                }
            }
        }
        return arr;
    }

    public static void main(String[] args) {
        int [] arr = {4,1,6,8,2,4,7,3};
        int[] ints = doInsertionSort(arr);
        for(int a : ints)
        {
            System.out.print(a+ " ");
        }
    }
}
