package DS.BinaryTree.a29TopView;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.*;

public class TopViewBT {

    TreeNode root;
    public static TreeMap<Integer, Integer> ht = new TreeMap<>();



    public void insertItem(int data) {

        TreeNode newNode = new TreeNode();
        newNode.left = null;
        newNode.right = null;
        newNode.val = data;

        if (root == null) {
            root = newNode;
            return;
        }

        TreeNode p = root;
        TreeNode c = root;

        while (c != null) {
            p = c;
            if (data < c.val) {
                c = c.left;
            } else {
                c = c.right;
            }
        }

        if (data < p.val) {
            p.left = newNode;
        } else {
            p.right = newNode;
        }
    }

    public class QueuePack {
        int level;
        TreeNode tnode;

        public QueuePack(int level, TreeNode tnode) {
            this.level = level;
            this.tnode = tnode;
        }
    }

    public void topView(TreeNode root, int level) {
        if (root == null) {
            return;
        }

        Queue<QueuePack> queue = new LinkedList<>();

        queue.add(new QueuePack(level, root));

        while (!queue.isEmpty()) {
            QueuePack q = queue.poll();

            TreeNode tnode = q.tnode;
            int lvl = q.level;

            if (ht.containsKey(lvl)) {

            } else {
                System.out.print(tnode.val + " ");
                ht.put(lvl, tnode.val);
            }
            // add the left and right children of visiting nodes to the queue
            if (tnode.left != null) {
                queue.add(new QueuePack(lvl + 1, tnode.left));
            }
            if (tnode.right != null) {
                queue.add(new QueuePack(lvl - 1, tnode.right));
            }
        }
    }

    public static void main(String[] args) {
        TopViewBT topViewBT = new TopViewBT();
        topViewBT.insertItem(8);
        topViewBT.insertItem(3);
        topViewBT.insertItem(10);
        topViewBT.insertItem(1);
        topViewBT.insertItem(6);
        topViewBT.insertItem(14);
        topViewBT.insertItem(4);
        topViewBT.insertItem(7);
        topViewBT.insertItem(13);

        topViewBT.topView(topViewBT.root, 0);
    }
}