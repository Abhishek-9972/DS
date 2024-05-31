package ds.sorting.mergeSort;

public class MergeSort {
    private int[] array;
    private int[] tempArray;
    private int length;

    public static void main(String[] args) {
        int[] inputArr = {45, 23, 11, 89, 77, 98, 4, 28, 65, 43};
        MergeSort mergeSort = new MergeSort();
        mergeSort.sort(inputArr);
        for (int i : inputArr) {
            System.out.print(i + " ");
        }
    }

    private void sort(int[] inputArr) {
        this.array = inputArr;
        this.length = inputArr.length;
        this.tempArray = new int[length];
        doMergeSort(0, length - 1);
    }

    private void doMergeSort(int lowerIndex, int higherIndex) {
        if (lowerIndex < higherIndex) {
            int middle = (lowerIndex + higherIndex) / 2;
            doMergeSort(lowerIndex, middle);
            doMergeSort(middle + 1, higherIndex);
            mergeParts(lowerIndex, middle, higherIndex);
        }
    }

    private void mergeParts(int lowerIndex, int middle, int higherIndex) {
        for (int i = lowerIndex; i <= higherIndex; i++) {
            tempArray[i] = array[i];
        }

        int i = lowerIndex;
        int j = middle + 1;
        int k = lowerIndex;
        while (i <= middle && j <= higherIndex) {
            if (tempArray[i] <= tempArray[j]) {
                array[k] = tempArray[i];
                i++;
            } else {
                array[k] = tempArray[j];
                j++;
            }
            k++;
        }
        while (i <= middle) {
            array[k] = tempArray[i];
            k++;
            i++;
        }
        while (j <= higherIndex) {
            array[k] = tempArray[j];
            k++;
            j++;
        }
    }
}
