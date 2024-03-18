package ds.stack.misc;

import java.util.Stack;

/**
 *
 * 1) Create one temporary stack
 * 2) Pop from input stack
 * 3) Check if temp stack is not empty and tempStack.peek > tmp (popped element)
 *          If yes then push it in the input stack
 * 4) Push the temp into temp stack
 * 5) Return the temp stack
 */
public class StackSort {

    private static Stack<Integer> sortStack(Stack<Integer> input) {
        Stack<Integer> tmpStack = new Stack<>();
        while (!input.isEmpty()) {
            int tmp = input.pop();
            System.out.println("Element taken out : " + tmp);
            while (!tmpStack.isEmpty() && tmpStack.peek() > tmp) {
                input.push(tmpStack.pop());
            }
            tmpStack.push(tmp);
            System.out.println("Input : " + input);
            System.out.println("TmpStack : " + tmpStack);
        }
        return tmpStack;
    }

    public static void main(String[] args) {
        Stack<Integer> input = new Stack<>();
        input.add(34);
        input.add(2);
        input.add(14);
        input.add(54);
        input.add(4);
        System.out.println("Final Sorted List : "+ sortStack(input));

    }
}


/**
 *
 * Input -> 34 2 14 54 4
 *
 * Step 1 :
 * input 34 2 14 54
 * tmp = 4
 * temp 4
 *
 * Step 2 :
 * input 34 2 14
 * tmp = 54
 * temp 4 54
 *
 * Step 3 :
 *
 * input 34 2 54
 * tmp = 14
 * temp 4 14
 *
 * Step 4 :
 *
 * input 34 2
 * tmp = 54
 * temp = 4 14 54
 *
 * Step 5 :
 *
 * input 34 54
 * tmp = 2
 * temp = 4 14 2
 *
 */