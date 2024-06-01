package DS.BinaryTree.a03ValidateBinarySearchTree;

import DS.BinaryTree.a01Traversal.TreeNode;

//https://leetcode.com/problems/validate-binary-search-tree/description/

public class ValidateBinarySearchTree {
    public boolean isValidBST(TreeNode root) {
        if (root.left == null && root.right == null) {
            return true;
        }
        return isBST(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public boolean isBST(TreeNode root, long min, long max) {
        if (root == null) {
            return true;
        }
        if (root.val <= min || root.val >= max) {
            return false;
        }
        return isBST(root.left, min, root.val) && isBST(root.right, root.val, max);
    }
}