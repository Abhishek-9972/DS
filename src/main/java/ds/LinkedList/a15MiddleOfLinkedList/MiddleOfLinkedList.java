package ds.LinkedList.a15MiddleOfLinkedList;

import ds.LinkedList.ListNode;

public class MiddleOfLinkedList{
    public ListNode middleNode(ListNode head) {
        ListNode fastPtr = head;
        ListNode slowPtr = head;

        while(fastPtr!=null && fastPtr.next!=null){
            slowPtr = slowPtr.next;
            fastPtr = fastPtr.next.next;
        }

        return slowPtr;
    }
}