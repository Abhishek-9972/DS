package ds.BinaryTree.a18BinaryTreeRightSideView;

import ds.BinaryTree.a01Traversal.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BinaryTreeRightSideView {
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> ans = new ArrayList<>();
        if (root == null)
            return ans;
        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (queue.isEmpty() == false) {
            int size = queue.size();
            for(int i=0; i<size; i++) {
                TreeNode fnt = queue.poll();
                if(i==size-1){
                    ans.add(fnt.val);
                }
                if (fnt.left != null)
                    queue.add(fnt.left);
                if (fnt.right != null)
                    queue.add(fnt.right);
            }
        }
        return ans;
    }
}