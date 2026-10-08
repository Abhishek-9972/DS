package DS.BinaryTree.a07MinimumDepthOfBinaryTree;

import DS.BinaryTree.a01Traversal.TreeNode;

public class MinimumDepthOfBinaryTree {
    public int minDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = minDepth(root.left);
        int right = minDepth(root.right);

        // If one child is missing, we must take the depth of the existing child.
        // Math.max() ignores the 0 from the missing child.
        if (left == 0 || right == 0)
            return 1 + Math.max(left, right);

        return 1 + Math.min(left, right);
    }
}
