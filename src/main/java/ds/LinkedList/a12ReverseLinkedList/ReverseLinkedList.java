package ds.LinkedList.a12ReverseLinkedList;

import ds.LinkedList.ListNode;

public class ReverseLinkedList {
        public ListNode reverseList(ListNode head) {
            ListNode previous = null;
            ListNode current = head;
            ListNode next = null;

            while(current!=null){
                next = current.next;
                current.next = previous;
                previous = current;
                current = next;
            }
            return previous;
        }
    }

