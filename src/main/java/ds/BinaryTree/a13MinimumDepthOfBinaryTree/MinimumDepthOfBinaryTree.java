package ds.BinaryTree.a13MinimumDepthOfBinaryTree;

import ds.BinaryTree.a01Traversal.TreeNode;

public class MinimumDepthOfBinaryTree {
    public int minDepth(TreeNode root) {
        return findMinDepth(root);
    }
    int findMinDepth(TreeNode root){
        if(root==null){
            return 0;
        }else if(root.left==null && root.right==null){
            return 1;
        }else if(root.left==null){
            return findMinDepth(root.right)+1;
        }else if(root.right==null){
            return findMinDepth(root.left)+1;
        }
        return Math.min(findMinDepth(root.left),findMinDepth(root.right))+1;
    }
}