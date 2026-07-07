package DS.BinaryTree.a16SumRootToLeafNumbers;

import DS.BinaryTree.a01Traversal.TreeNode;

public class SumRootToLeafNumbers {

    private int sum = 0;

    public int sumNumbers(TreeNode root) {

        dfs(root, 0);
        return sum;
    }

    private void dfs(TreeNode root, int current) {

        if (root == null)
            return;

        current = current * 10 + root.val;

        if (root.left == null && root.right == null) {
            sum += current;
            return;
        }

        dfs(root.left, current);
        dfs(root.right, current);
    }
}