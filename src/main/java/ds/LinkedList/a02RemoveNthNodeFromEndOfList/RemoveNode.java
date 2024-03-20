package ds.LinkedList.a02RemoveNthNodeFromEndOfList;


import ds.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/remove-nth-node-from-end-of-list/description/
 */
class RemoveNode {
    public ListNode removeNthFromEnd(ListNode head, int n) {

        int length = count(head);
        if(length == 0){
            return head;
        }else if(length-n == 0){
            return head.next;
        }
        ListNode cur = head;
        for(int i =0; i<length; i++){

            if(i == (length - n -1)){
                cur.next = cur.next.next;
                break;
            }
            cur = cur.next;
        }
        return head;
    }

    public int count(ListNode head){
        ListNode cur = head;
        int len = 0;
        while(cur!=null){
            cur = cur.next;
            len++;
        }
        return len;
    }
}