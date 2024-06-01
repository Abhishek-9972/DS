package DS.LinkedList.a01AddTwoNumbers;

import DS.LinkedList.ListNode;

/**
 * https://leetcode.com/problems/add-two-numbers/description/
 */
public class AddTwoNumbers {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummyHead = new ListNode(0);
        ListNode l3 = dummyHead;
        int carry = 0;

        while (l1 != null || l2 != null || carry != 0) {
            int l1Val = (l1 != null) ? l1.val : 0;
            int l2Val = (l2 != null) ? l2.val : 0;

            int sum = l1Val + l2Val + carry;
            carry = sum / 10;
            int lastDigit = sum % 10;

            ListNode newNode = new ListNode(lastDigit);
            l3.next = newNode;
            l3 = l3.next;

            if (l1 != null) {
                l1 = l1.next;
            }
            if (l2 != null) {
                l2 = l2.next;
            }

        }

        return dummyHead.next;

    }
}
