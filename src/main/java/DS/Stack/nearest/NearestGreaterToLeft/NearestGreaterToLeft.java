package DS.Stack.nearest.NearestGreaterToLeft;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class NearestGreaterToLeft {
    private List<Integer> getNextGreaterToLeft(int[] arr) {
        List<Integer> resultList = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i<= arr.length - 1; i++) {
            if (stack.isEmpty()) {
                resultList.add(-1);
            } else if (stack.peek() > arr[i]) {
                resultList.add(stack.peek());
            } else if (stack.peek() <= arr[i]) {
                while (stack.size() > 0 && stack.peek() <= arr[i]) {
                    stack.pop();
                }
                if (stack.size() == 0) {
                    resultList.add(-1);
                } else {
                    resultList.add(stack.peek());
                }
            }
            stack.push(arr[i]);


        }
        return resultList;
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 2, 4};
        NearestGreaterToLeft nearestGreaterToLeft = new NearestGreaterToLeft();
        List<Integer> nextGreaterToLeft1 = nearestGreaterToLeft.getNextGreaterToLeft(arr);
        System.out.println(nextGreaterToLeft1);
    }

}
