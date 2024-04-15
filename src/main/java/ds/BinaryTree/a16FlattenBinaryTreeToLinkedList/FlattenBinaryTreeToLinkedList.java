package ds.BinaryTree.a16FlattenBinaryTreeToLinkedList;

import ds.BinaryTree.a01Traversal.TreeNode;

public class FlattenBinaryTreeToLinkedList {
    private TreeNode prev = null;

    public void flatten(TreeNode root) {
        if (root == null)
            return;
        flatten(root.right);
        flatten(root.left);
        root.right = prev;
        root.left = null;
        prev = root;
    }
}