package DS.BinaryTree.a31BinaryTreeMaxPathSum;

import DS.BinaryTree.a01Traversal.TreeNode;

public class BinaryTreeMaxPathSum {
    int max = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        helper(root);
        return max;
    }

    // helper returns the max branch
    // plus current node's value
    int helper(TreeNode root) {
        if (root == null) return 0;

        int left = Math.max(helper(root.left), 0);
        int right = Math.max(helper(root.right), 0);

        max = Math.max(max, root.val + left + right);

        return root.val + Math.max(left, right);
    }
}


/**
 Hi @d20@15, you can imagine the helper( ) function goes from the bottom of the tree to the top, it's in post-order manner.

 At every node, we need to make a decision, if the sum comes from the left path larger than the right path, we pick the left path and plus the current node's value, this recursion goes all the way up to the root node.
 */