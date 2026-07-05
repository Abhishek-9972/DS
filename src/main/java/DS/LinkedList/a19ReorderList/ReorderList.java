package DS.LinkedList.a19ReorderList;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/reorder-list/
 */
public class ReorderList {

    public void reorderList(ListNode head) {

        if (head == null || head.next == null) {
            return;
        }

        ListNode middle = middleNode(head);

        ListNode second = reverseList(middle.next);

        // Break the list into two halves
        middle.next = null;

        ListNode first = head;

        while (second != null) {

            ListNode nextFirst = first.next;
            ListNode nextSecond = second.next;

            first.next = second;
            second.next = nextFirst;

            first = nextFirst;
            second = nextSecond;
        }
    }

    public ListNode middleNode(ListNode head) {

        ListNode slow = head;
        ListNode fast = head;

        // Return first middle for even length
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow;
    }

    public ListNode reverseList(ListNode head) {

        ListNode previous = null;
        ListNode current = head;

        while (current != null) {

            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }

        return previous;
    }
}