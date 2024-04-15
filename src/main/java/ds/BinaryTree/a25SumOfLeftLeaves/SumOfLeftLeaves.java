package ds.BinaryTree.a25SumOfLeftLeaves;

import ds.BinaryTree.a01Traversal.TreeNode;

public class SumOfLeftLeaves {
    int sum=0;
    public int sumOfLeftLeaves(TreeNode root) {
        if(root==null){
            return 0;
        }
        dfs(root);
        return sum;
    }
    private void dfs(TreeNode root){
        if(root.left!=null){
            if(root.left.left==null && root.left.right==null){
                sum+=root.left.val;
            }
            else{
                dfs(root.left);
            }
        }
        if(root.right!=null){
            dfs(root.right);
        }
    }
}