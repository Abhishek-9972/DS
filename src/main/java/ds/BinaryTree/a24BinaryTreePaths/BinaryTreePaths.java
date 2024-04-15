package ds.BinaryTree.a24BinaryTreePaths;

import ds.BinaryTree.a01Traversal.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class BinaryTreePaths {
    public List<String> binaryTreePaths(TreeNode root) {
        List<String> paths = new ArrayList<>();
        if (root == null) {
            return paths;
        }
        binaryTreePathsDfs(root, "", paths);
        return paths;
    }

    private void binaryTreePathsDfs(TreeNode root, String path, List<String> paths) {
        path += root.val;

        if (root.left == null && root.right == null) {
            paths.add(path);
            return;
        }

        if (root.left != null) {
            binaryTreePathsDfs(root.left, path + "->", paths);
        }

        if (root.right != null) {
            binaryTreePathsDfs(root.right, path + "->", paths);
        }
    }
}