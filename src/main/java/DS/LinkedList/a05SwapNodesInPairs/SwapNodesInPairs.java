package DS.LinkedList.a05SwapNodesInPairs;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/swap-nodes-in-pairs/description/
 */
public class SwapNodesInPairs {
    public ListNode swapPairs(ListNode head) {
        ListNode dummyHead = new ListNode(0);
        dummyHead.next = head;

        ListNode current = dummyHead;

        while (current.next != null && current.next.next != null) {
            ListNode firstNode = current.next;
            ListNode secondNode = current.next.next;
            firstNode.next = secondNode.next;
            current.next = secondNode;
            secondNode.next = firstNode;
            current = firstNode;
        }
        return dummyHead.next;
    }
}
