package DS.BinaryTree.a28SiblingNode;

import DS.BinaryTree.a01Traversal.TreeNode;

public class SiblingNode {
    public TreeNode getSiblingNode(TreeNode node, int val) {
        if (node == null || node.val == val) {
            return null;
        }

        TreeNode parentNode = null;

        while (node != null) {
            if (val < node.val) {
                parentNode = node;
                node = node.left;
            } else if (val > node.val) {
                parentNode = node;
                node = node.right;
            } else {
                break;
            }
        }

        if (parentNode.left != null && val == parentNode.left.val) {
            return parentNode.right;
        }

        if (parentNode.right != null && val == parentNode.right.val) {
            return parentNode.left;
        }

        return null;
    }
}
