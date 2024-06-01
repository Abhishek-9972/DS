package DS.Stack.nearest.stockSpan;


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StockSpan {
    private List<Integer> getNextGreaterToLeft(int[] arr) {
        List<Integer> resultList = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        int count = 1;

        for (int i = 0; i <= arr.length - 1; i++) {
            if (stack.isEmpty()) {
                resultList.add(1);
            } else if (stack.peek() > arr[i]) {
                resultList.add(1);
            } else if (stack.peek() <= arr[i]) {
                count = 1;
                while (stack.size() > 0 && stack.peek() <= arr[i]) {
                    stack.pop();
                    count++;
                }
                if (stack.size() == 0) {
                    resultList.add(1);
                } else {
                    resultList.add(count);
                }
            }
            stack.push(arr[i]);
        }
        return resultList;
    }

    public static void main(String[] args) {
        int[] arr = {100, 80, 60, 70, 60, 75, 85};
        StockSpan stockSpan = new StockSpan();
        List<Integer> nextGreaterToLeft1 = stockSpan.getNextGreaterToLeft(arr);
        System.out.println(nextGreaterToLeft1);
    }
}
