package DS.Array.a07RemoveDuplicatesFromSortedArray;

public class RemoveDuplicates {
    public int removeDuplicates(int[] arr) {
        int counter = 0;
        for(int i=0; i<arr.length;i++){
            int start = arr[i];
            while(i+1<arr.length && arr[i+1]==arr[i]){
                i++;
            }
            arr[counter]=arr[i];
            counter++;
        }
        return counter;
    }
}