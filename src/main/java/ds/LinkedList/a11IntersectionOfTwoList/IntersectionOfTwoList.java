package ds.LinkedList.a11IntersectionOfTwoList;

import ds.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/intersection-of-two-linked-lists/description/
 */
public class IntersectionOfTwoList {
    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {

        if(headA == null || headB == null) return null;


        ListNode a = headA;
        ListNode b = headB;

        while(a!=b){
            a = a==null ? headB : a.next;
            b = b==null ? headA : b.next;
        }

        return a;
    }
}