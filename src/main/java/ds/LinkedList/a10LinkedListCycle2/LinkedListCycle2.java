package ds.LinkedList.a10LinkedListCycle2;

import ds.LinkedList.ListNode;

public class LinkedListCycle2 {
    public ListNode detectCycle(ListNode head) {
        if(head == null){
            return null;
        }
        ListNode fastPtr = head;
        ListNode slowPtr = head;

        while(fastPtr.next!=null && fastPtr.next.next!=null ){

            fastPtr = fastPtr.next.next;
            slowPtr = slowPtr.next;

            if(fastPtr == slowPtr){
                slowPtr = head;
                while(slowPtr!=fastPtr){
                    slowPtr = slowPtr.next;
                    fastPtr = fastPtr.next;
                }
                return slowPtr;
            }
        }

        return null;
    }
}