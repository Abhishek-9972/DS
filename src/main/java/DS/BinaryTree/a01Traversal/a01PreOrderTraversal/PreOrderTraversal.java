package DS.BinaryTree.a01Traversal.a01PreOrderTraversal;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class PreOrderTraversal {
    List<Integer> result = new ArrayList<>();
    public List<Integer> preorderTraversal(TreeNode root) {
        if(root == null){return result;}
        result.add(root.val);
        preorderTraversal(root.left);
        preorderTraversal(root.right);
        return result;
    }
}