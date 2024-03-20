package ds.LinkedList.a08PartitionList;

import ds.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/partition-list/description/
 */
public class PartitionList {
    public ListNode partition(ListNode head, int x) {
        ListNode beforeHead = new ListNode(0);
        ListNode before = beforeHead;
        ListNode afterHead = new ListNode(0);
        ListNode after = afterHead;
        ListNode temp = head;

        while (head!=null)
        {
            if(head.val<x)
            {
                before.next = head;
                before = before.next;
            }
            else{
                after.next = head;
                after = after.next;
            }
            head = head.next;
        }
        after.next = null;
        before.next = afterHead.next;
        return beforeHead.next;
    }
}