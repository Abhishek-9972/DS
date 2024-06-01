package DS.BinaryTree.a21KthSmallestElementInBST;

import DS.BinaryTree.a01Traversal.TreeNode;

import java.util.Collections;
import java.util.PriorityQueue;

public class KthSmallestElementInBST {
    PriorityQueue<Integer> priorityQueue = new PriorityQueue<Integer>(Collections.reverseOrder());
    public int kthSmallest(TreeNode root, int k) {
        if(root == null) { return 0;}
        kthSmallestPQ(root,k);
        System.out.println(priorityQueue);
        return priorityQueue.size()>0 ? priorityQueue.poll() : 0;
    }

    public void kthSmallestPQ(TreeNode root, int k) {
        if(root == null) { return;}
        priorityQueue.add(root.val);
        if(priorityQueue.size()>k){priorityQueue.poll();}
        kthSmallestPQ(root.left,k);
        kthSmallestPQ(root.right,k);
    }
}