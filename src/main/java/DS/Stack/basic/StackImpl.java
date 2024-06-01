package DS.Stack.basic;

public class StackImpl {

    private int stackSize;
    private char[] stackArr;
    private int top;

    public StackImpl(int stackSize) {
        this.stackSize = stackSize;
        stackArr = new char[stackSize];
        top = -1;
    }

    public void push(char entry) throws Exception {
        if (isStackFull()) {
            throw new Exception("Stack Overflow Error");
        } else {
            stackArr[++top] = entry;
        }
    }

    public char pop() throws Exception {
        if(isStackEmpty()){
            throw new Exception("Stack Overflow Error");
        }
        else{
            return stackArr[top--];
        }
    }

    public char peek() throws Exception {
        if(isStackEmpty()){
            throw new Exception("Stack Overflow Error");
        }
        else{
            return stackArr[top];
        }
    }

    private boolean isStackFull() {
        return top == stackSize - 1;
    }

    private boolean isStackEmpty() {
        return top == -1;
    }

    public static void main(String[] args) throws Exception {
        StackImpl stack = new StackImpl(10);
        stack.push('a');
        stack.push('b');
        stack.push('c');
        stack.push('d');
        stack.push('e');
        System.out.println(stack.pop());
        System.out.println(stack.peek());

    }
}
