package DS.BinaryTree.a08BalancedBinaryTree;

import DS.BinaryTree.a01Traversal.TreeNode;

/**
 * A Balanced Binary Tree is a binary tree in which:
 *
 * For every node, the difference between the height of its left subtree and right subtree is at most 1.
 *
 * https://www.youtube.com/watch?v=Yt50Jfbd8Po
 */
public class BalancedBinaryTree {

    public boolean isBalanced(TreeNode root) {
        return height(root) != -1;
    }

    private int height(TreeNode root) {

        if (root == null)
            return 0;

        int left = height(root.left);
        if (left == -1)
            return -1;

        int right = height(root.right);
        if (right == -1)
            return -1;

        if (Math.abs(left - right) > 1)
            return -1;

        return Math.max(left, right) + 1;
    }
}