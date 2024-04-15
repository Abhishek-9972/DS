package ds.BinaryTree.a19CountCompleteTreeNodes;

import ds.BinaryTree.a01Traversal.TreeNode;

public class CountCompleteTreeNodes {
    int sum = 0;
    public int countNodes(TreeNode root) {
        if(root == null){return 0;}
        sum++;
        countNodes(root.left);
        countNodes(root.right);
        return sum;
    }
}