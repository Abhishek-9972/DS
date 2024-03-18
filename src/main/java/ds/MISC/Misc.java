package ds.MISC;

import java.util.*;

public class Misc {
    public static void main(String[] args) {


        /**
         *
         * STACK
         *
         * push(1)
         * pop()
         * peek()
         *
         *  | 3 |
         *  | 2 |
         *  | 1 |
         */

        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);

        System.out.println("Stack Pop: " + stack.pop());//3
        System.out.println("Stack Peek: " + stack.peek());//2

        /**
         *
         *
         * Queue
         *
         * add(1)
         * poll()
         * peek()
         *
         * | 1 | 2 | 3 |
         *
         */

        Queue<Integer> queue = new LinkedList<>();
        queue.add(1);
        queue.add(2);
        queue.add(3);

        System.out.println("Queue Poll: " + queue.poll());//1
        System.out.println("Queue Peek: " + queue.peek());//2

        /**
         *
         *
         * PriorityQueue
         *
         * add(1)
         * poll()
         * peek()
         *
         * PriorityQueue pq = new PriorityQueue<>()
         * Asc
         * | 1 | 2 | 3 | 4 |
         *
         * PriorityQueue pqR = new PriorityQueue<>(Collections.reverseOrder())
         * Desc
         * | 4 | 3 | 2 | 1 |
         *
         */

        PriorityQueue<Integer> pq = new PriorityQueue();
        pq.add(2);
        pq.add(4);
        pq.add(1);
        pq.add(3);

        System.out.println("PriorityQueue Poll: " + pq.poll());//1
        System.out.println("PriorityQueue Peek: " + pq.peek());//2

        PriorityQueue<Integer> pqR = new PriorityQueue(Collections.reverseOrder());
        pqR.add(2);
        pqR.add(4);
        pqR.add(1);
        pqR.add(3);

        System.out.println("PriorityQueue Poll: " + pqR.poll());//4
        System.out.println("PriorityQueue Peek: " + pqR.peek());//3

        /**
         *
         *
         * Deque
         *
         * addFirst(1)
         * pollFirst()
         * peekFirst()
         *
         * | 2 | 1 | 3 |
         *
         */
        Deque<Integer> deque = new LinkedList<>();
        deque.addFirst(1);
        deque.addFirst(2);
        deque.addLast(3);
        System.out.println("Deque First : "+deque.pollFirst());//2
        System.out.println("Deque Last : "+deque.pollLast());//3


        /**
         * OUTPUT
         *
         * Stack Pop: 3
         * Stack Peek: 2
         * Queue Poll: 1
         * Queue Peek: 2
         * PriorityQueue Poll: 1
         * PriorityQueue Peek: 2
         * PriorityQueue Poll: 4
         * PriorityQueue Peek: 3
         * Deque First : 2
         * Deque Last : 3
         */

    }
}
