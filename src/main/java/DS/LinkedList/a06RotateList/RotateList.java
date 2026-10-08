package DS.LinkedList.a06RotateList;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/rotate-list/
 */
public class RotateList {

    public ListNode rotateRight(ListNode head, int k) {

        // Empty list, single node, or no rotation
        if (head == null || head.next == null || k == 0) {
            return head;
        }

        // Find length and current tail
        int length = 1;
        ListNode tail = head;

        while (tail.next != null) {
            tail = tail.next;
            length++;
        }

        // Rotating length times brings us back to the same list
        k %= length;

        if (k == 0) {
            return head;
        }

        // Connect tail to head to make the list circular
        tail.next = head;

        // New tail will be at position length - k
        int stepsToNewTail = length - k;

        ListNode newTail = head;

        for (int i = 1; i < stepsToNewTail; i++) {
            newTail = newTail.next;
        }

        // Node after newTail becomes the new head
        ListNode newHead = newTail.next;

        // Break the circular list
        newTail.next = null;

        return newHead;
    }
}