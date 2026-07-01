package DS.LinkedList.a06RotateList;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/rotate-list/description/
 */
public class RotateList {
    public ListNode rotateRight(ListNode head, int k) {

        if (head == null || head.next == null || k == 0) {
            return head;
        }

        ListNode tail = head;
        int len = 1;

        while (tail.next != null) {
            len++;
            tail = tail.next;
        }

        k %= len;

        if (k == 0) {
            return head;
        }

        int stepsToNewTail = len - k;

        tail.next = head;

        while (stepsToNewTail-- > 0) {
            tail = tail.next;
        }

        head = tail.next;
        tail.next = null;

        return head;
    }
}