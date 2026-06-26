package DS.BinaryTree.a11KthSmallestElementInBST;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.Collections;
import java.util.PriorityQueue;

public class KthSmallestElementInBST {

    private int count = 0;

    public int kthSmallest(TreeNode root, int k) {

        if (root == null)
            return -1;

        int left = kthSmallest(root.left, k);
        if (left != -1)
            return left;

        count++;
        if (count == k)
            return root.val;

        return kthSmallest(root.right, k);
    }
}