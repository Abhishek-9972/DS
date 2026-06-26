package DS.BinaryTree.a14PathSum2;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class PathSum2 {

    public List<List<Integer>> pathSum(TreeNode root, int targetSum) {

        List<List<Integer>> result = new ArrayList<>();

        backtrack(root, targetSum, new ArrayList<>(), result);

        return result;
    }

    private void backtrack(TreeNode root, int targetSum, List<Integer> partial, List<List<Integer>> result) {

        if (root == null)
            return;

        partial.add(root.val);

        if (root.left == null && root.right == null && targetSum == root.val) {
            result.add(new ArrayList<>(partial));
        } else {
            backtrack(root.left, targetSum - root.val, partial, result);
            backtrack(root.right, targetSum - root.val, partial, result);
        }

        partial.remove(partial.size() - 1);
    }
}