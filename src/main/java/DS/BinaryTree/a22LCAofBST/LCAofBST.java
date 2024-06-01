package DS.BinaryTree.a22LCAofBST;

import DS.BinaryTree.a01Traversal.TreeNode;

public class LCAofBST {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {

        if(root==null) { return null;}

        if ( root == p || root == q )
            return root;

        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);

        // if Left is NULL, Right will be our answer
        if ( left == null ) return right;

            // if Rigth is NULL, Left be our answer
        else if ( right == null ) return left;

            // if both are NOT NULL, then that node is our answer
        else
            return root;
    }
}