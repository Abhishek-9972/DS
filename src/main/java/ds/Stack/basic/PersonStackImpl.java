package ds.Stack.basic;

public class PersonStackImpl {

    private int stackSize;
    private Person[] stackArr;
    private int top;

    public PersonStackImpl(int stackSize) {
        this.stackSize = stackSize;
        stackArr = new Person[stackSize];
        top = -1;
    }

    public void push(Person entry) throws Exception {
        if (isStackFull()) {
            throw new Exception("Stack Overflow Error");
        } else {
            stackArr[++top] = entry;
        }
    }

    public Person pop() throws Exception {
        if(isStackEmpty()){
            throw new Exception("Stack Overflow Error");
        }
        else{
            return stackArr[top--];
        }
    }

    public Person peek() throws Exception {
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
        PersonStackImpl stack = new PersonStackImpl(10);
        stack.push(new Person(1, "Jack"));
        stack.push(new Person(2, "Abinash"));
        stack.push(new Person(3, "Amy"));
        stack.push(new Person(4, "Tom"));
        stack.push(new Person(5, "Harry"));
        System.out.println(stack.pop());
        System.out.println(stack.peek());

    }
}
