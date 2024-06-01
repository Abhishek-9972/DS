package DS.BinaryTree.a12BalancedBinaryTree;

import DS.BinaryTree.a01Traversal.TreeNode;

public class BalancedBinaryTree {
    public boolean isBalanced(TreeNode root) {
        if (root == null) return true;
        int heightDifference = height(root.left) - height(root.right);
        if (Math.abs(heightDifference) > 1) {
            return false;
        } else {
            return isBalanced(root.left) && isBalanced(root.right);
        }
    }

    public int height(TreeNode root) {
        if (root == null)
            return 0;

        int left = height(root.left);
        int right = height(root.right);

        return Math.max(left, right) + 1;
    }
}