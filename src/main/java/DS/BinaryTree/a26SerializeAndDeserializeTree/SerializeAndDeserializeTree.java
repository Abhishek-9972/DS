package DS.BinaryTree.a26SerializeAndDeserializeTree;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.LinkedList;
import java.util.Queue;

public class SerializeAndDeserializeTree {
    public String serialize(TreeNode root) {
        if (root == null) return "";
        Queue<TreeNode> q = new LinkedList<>();
        StringBuilder res = new StringBuilder();
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode node = q.poll();
            if (node == null) {
                res.append("n ");
                continue;
            }
            res.append(node.val + " ");
            q.add(node.left);
            q.add(node.right);
        }
        return res.toString();
    }

    public TreeNode deserialize(String data) {

        if (data == null || data.isEmpty()) {
            return null;
        }

        String[] values = data.split(" ");

        TreeNode root = new TreeNode(Integer.parseInt(values[0]));

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        int index = 1;

        while (!queue.isEmpty()) {

            TreeNode parent = queue.poll();

            // Left child
            if (!values[index].equals("n")) {
                TreeNode left = new TreeNode(Integer.parseInt(values[index]));
                parent.left = left;
                queue.add(left);
            }
            index++;

            // Right child
            if (!values[index].equals("n")) {
                TreeNode right = new TreeNode(Integer.parseInt(values[index]));
                parent.right = right;
                queue.add(right);
            }
            index++;
        }

        return root;
    }
}

// Your Codec object will be instantiated and called as such:
// Codec ser = new Codec();
// Codec deser = new Codec();
// TreeNode ans = deser.deserialize(ser.serialize(root));

/**

 Here I use typical BFS method to handle a binary tree. I use string n to represent null values. The string of the binary tree in the example will be "1 2 3 n n 4 5 n n n n ".

 When deserialize the string, I assign left and right child for each not-null node, and add the not-null children to the queue, waiting to be handled later.
 */