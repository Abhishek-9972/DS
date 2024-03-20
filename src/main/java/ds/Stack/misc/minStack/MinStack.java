package ds.Stack.misc.minStack;

import java.util.Stack;

public class MinStack {
    Stack<Integer> stack = new Stack<>();
    Stack<Integer> minValues = new Stack<>();

    public MinStack() {

    }

    public void push(int x) {
        if(minValues.isEmpty() || x<=minValues.peek())
        {
            minValues.push(x);
        }
        stack.push(x);
    }

    public void pop() {
        if(stack.peek().equals(minValues.peek()))
        {
            minValues.pop();
        }
        stack.pop();
    }

    public int top() {
        return stack.peek();
    }

    public int getMin() {
        return minValues.peek();
    }

    public static void main(String[] args) {
        MinStack obj = new MinStack();
        obj.push(4);
        obj.push(3);
        System.out.println(obj.getMin());
        obj.pop();
        int param_3 = obj.top();
        int param_4 = obj.getMin();
        System.out.println(param_3);
        System.out.println(param_4);
    }
}
