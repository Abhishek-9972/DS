package ds.LinkedList.a13PalindromeLinkedList;

import ds.LinkedList.ListNode;

public class PalindromeLinkedList {
    public boolean isPalindrome(ListNode head) {
        ListNode actualHead = head;
        ListNode cur = head;
        ListNode middle = middleNode(cur);
        System.out.println(middle.val);
        ListNode secondHead = reverseList(middle);
        System.out.println(secondHead.val);
        System.out.println(actualHead.val);


        while(secondHead!=null){
            if(actualHead.val != secondHead.val){
                return false;
            }
            actualHead = actualHead.next;
            secondHead = secondHead.next;
        }
        return true;
    }

    public ListNode middleNode(ListNode head) {
        ListNode fastPtr = head;
        ListNode slowPtr = head;

        while(fastPtr!=null && fastPtr.next!=null){
            slowPtr = slowPtr.next;
            fastPtr = fastPtr.next.next;
        }

        return slowPtr;
    }

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