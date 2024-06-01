package DS.BinaryTree.a11SortedArrayToBinarySearchTree;

import DS.BinaryTree.a01Traversal.TreeNode;

public class SortedArrayToBinarySearchTree {
    public TreeNode sortedArrayToBST(int[] nums) {
        return sortedArrayToBst(nums, 0, nums.length-1);
    }

    private TreeNode sortedArrayToBst(int[] arr, int start, int end) {
        if (start > end) {
            return null;
        }
        int mid = (start + end) / 2;
        TreeNode root = new TreeNode();
        root.val = arr[mid];
        root.left = sortedArrayToBst(arr, start, mid - 1);
        root.right = sortedArrayToBst(arr, mid + 1, end);
        return root;
    }
}