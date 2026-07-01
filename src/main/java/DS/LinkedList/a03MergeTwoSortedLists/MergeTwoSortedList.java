package DS.LinkedList.a03MergeTwoSortedLists;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/merge-two-sorted-lists/description/
 */
class MergeTwoSortedList {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode dummyNode = new ListNode(0);
        ListNode current = dummyNode;
        while(list1!=null && list2!=null){

            if(list1.val <= list2.val){
                current.next = list1;
                list1 = list1.next;
            } else {
                current.next = list2;
                list2 =  list2.next;
            }

            current = current.next;
        }
        current.next = list1==null ? list2: list1;
        return dummyNode.next;
    }
}