package DS.LinkedList.a19ReorderList;

import DS.LinkedList.ListNode;

public class ReorderList {

    static ListNode reverse(ListNode head) {
        ListNode curr = head;
        ListNode after = null;
        ListNode prev = null;

        while (curr != null) {
            after = curr.next;
            curr.next = prev;
            prev = curr;
            curr = after;
        }
        return prev;
    }

    public void reorderList(ListNode head) {
        ListNode t = head;

        ListNode slow = head;
        ListNode fast = head.next;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        ListNode left = head;
        ListNode right = reverse(slow);
        ListNode nextL, nextR;
        while (left != null && right != null) {
            nextL = left.next;
            left.next = right;
            nextR = right.next;
            right.next = nextL;
            left = nextL;
            right = nextR;
        }
    }
}