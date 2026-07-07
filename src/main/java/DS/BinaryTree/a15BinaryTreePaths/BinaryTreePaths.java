package DS.BinaryTree.a15BinaryTreePaths;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class BinaryTreePaths {
    public List<String> binaryTreePaths(TreeNode root) {

        List<String> result = new ArrayList<>();

        backtrack(root, new ArrayList<>(), result);

        return result;
    }

    private void backtrack(TreeNode root,
                           List<Integer> partial,
                           List<String> result) {

        if (root == null)
            return;

        partial.add(root.val);

        if (root.left == null && root.right == null) {

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < partial.size(); i++) {

                sb.append(partial.get(i));

                if (i != partial.size() - 1)
                    sb.append("->");
            }

            result.add(sb.toString());

        } else {

            backtrack(root.left, partial, result);

            backtrack(root.right, partial, result);
        }

        partial.remove(partial.size() - 1);
    }
}
