package DS.LinkedList.a09LinkedListCycle;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/linked-list-cycle/description/
 */
public class LinkedListCycle {
    public boolean hasCycle(ListNode head) {

        if(head == null){
            return false;
        }
        ListNode fastPtr = head;
        ListNode slowPtr = head;

        while(fastPtr.next!=null && fastPtr.next.next!=null ){

            fastPtr = fastPtr.next.next;
            slowPtr = slowPtr.next;

            if(fastPtr == slowPtr){
                return true;
            }
        }

        return false;
    }
}