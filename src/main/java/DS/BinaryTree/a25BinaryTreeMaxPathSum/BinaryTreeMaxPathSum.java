package DS.BinaryTree.a25BinaryTreeMaxPathSum;

import DS.BinaryTree.a01Traversal.TreeNode;

/**
 * "At every node, I calculate the maximum gain from its left and right subtrees.
 * If a subtree contributes a negative sum, I ignore it by treating its contribution as zero.
 * The helper function returns the maximum gain that can be extended to the parent,
 * so it returns the current node plus the larger of the two gains. Meanwhile,
 * I update a global maximum using the current node plus both left and right gains,
 * since the maximum path may pass through the current node."
 */
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