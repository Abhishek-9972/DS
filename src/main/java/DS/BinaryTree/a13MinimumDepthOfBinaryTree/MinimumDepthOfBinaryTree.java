package DS.BinaryTree.a13MinimumDepthOfBinaryTree;

import DS.BinaryTree.a01Traversal.TreeNode;

public class MinimumDepthOfBinaryTree {
    public int minDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = minDepth(root.left);
        int right = minDepth(root.right);
        if (left == 0 || right == 0) {
            return 1 + Math.max(left, right);
        } else {
            return 1 + Math.min(left, right);
        }
    }
}
