package DS.Array.TwoPointers.a03RemoveDuplicatesFromSortedArray;

public class RemoveDuplicates {
    public int removeDuplicates(int[] arr) {
        int counter = 0;
        for(int i=0; i<arr.length;i++){
            while(i+1<arr.length && arr[i+1]==arr[i]){
                i++;
            }
            arr[counter]=arr[i];
            counter++;
        }
        return counter;
    }
}