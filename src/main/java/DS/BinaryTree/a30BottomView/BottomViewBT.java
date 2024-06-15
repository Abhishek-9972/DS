package DS.BinaryTree.a30BottomView;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

public class BottomViewBT {

    public static TreeMap<Integer, Integer> ht = new TreeMap<>();

    public class QueuePack {
        int level;
        TreeNode tnode;

        public QueuePack(int level, TreeNode tnode) {
            this.level = level;
            this.tnode = tnode;
        }
    }

    public void bottomView(TreeNode root, int level) {
        if (root == null)
            return;
        // create a queue for level order traversal
        Queue<QueuePack> queue = new LinkedList<>();
        // add root with level 0 (create a queue item pack)
        queue.add(new QueuePack(level, root));
        while (!queue.isEmpty()) {
            QueuePack q = queue.poll();
            // take out the items from the package
            TreeNode node = q.tnode;
            int lvl = q.level;

            // keep updating the Map so that it will have the last entry from
            // each level(vertically) and that will the bottom view of the tree
            ht.put(lvl, node.val);

            // add the left and right children of visiting nodes to the queue
            if (node.left != null) {
                queue.add(new QueuePack(lvl - 1, node.left));
            }
            if (node.right != null) {
                queue.add(new QueuePack(lvl + 1, node.right));
            }
        }
    }

    public static void display() { // print the bottom view.
        Set<Integer> keys = ht.keySet();
        for (Integer key : keys) {
            System.out.print(ht.get(key) + " ");
        }

    }

    public static void main(String[] args) {
        BottomViewBT bottomViewBT = new BottomViewBT();
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.right.right = new TreeNode(5);
        root.right.left = new TreeNode(6);
        root.right.right = new TreeNode(7);

        bottomViewBT.bottomView(root, 0);
        bottomViewBT.display();
        //bottomViewBT.levelOrderTraversal(bottomViewBT.root);
    }

}
