package DS.BinaryTree.a10BinaryTreeLevelOrderTraversal02;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BinaryTreeLevelOrderTraversal02 {
    public List<List<Integer>> levelOrderBottom(TreeNode root) {
        List<List<Integer>> ans = new LinkedList<>();
        List<Integer> smallans = new ArrayList<>();
        if (root == null)
            return ans;
        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (queue.isEmpty() == false) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode fnt = queue.poll();
                smallans.add(fnt.val);
                if (fnt.left != null)
                    queue.add(fnt.left);
                if (fnt.right != null)
                    queue.add(fnt.right);
            }
            ans.add(new ArrayList<>(smallans));
            smallans.clear();
        }

        List<List<Integer>> res = new LinkedList<>();
        for (int i = ans.size() - 1; i >= 0; i--) {
            res.add(ans.get(i));
        }
        return res;
    }
}