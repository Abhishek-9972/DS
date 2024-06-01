package DS.LinkedList.a06RotateList;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/rotate-list/description/
 */
public class RotateList {
    public ListNode rotateRight(ListNode head, int k) {
        ListNode temp = head;
        int len = 1;

        if (k == 0 || head == null || head.next == null) {
            return head;
        }

        while (temp.next != null) {
            len++;
            temp = temp.next;
        }

        k = k % len;

        int start = len - k;

        // create circle
        temp.next = head;

        while (start > 0) {
            temp = temp.next;
            start--;
        }

        head = temp.next;
        temp.next = null;

        return head;
    }
}