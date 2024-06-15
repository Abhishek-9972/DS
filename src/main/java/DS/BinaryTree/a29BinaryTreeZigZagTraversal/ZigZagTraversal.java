package DS.BinaryTree.a29BinaryTreeZigZagTraversal;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ZigZagTraversal {
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> ans = new ArrayList<>();
        LinkedList<Integer> smallans = new LinkedList<>();
        if (root == null)
            return ans;
        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int level = 0;
        while (queue.isEmpty() == false) {
            int size = queue.size();
            level++;
            for(int i=0; i<size; i++) {
                TreeNode fnt = queue.poll();
                if(level%2!=0){
                    smallans.add(fnt.val);
                } else {
                    smallans.addFirst(fnt.val);
                }
                if (fnt.left != null)
                    queue.add(fnt.left);
                if (fnt.right != null)
                    queue.add(fnt.right);
            }
            ans.add(new ArrayList<>(smallans));
            smallans.clear();
        }
        return ans;
    }
}