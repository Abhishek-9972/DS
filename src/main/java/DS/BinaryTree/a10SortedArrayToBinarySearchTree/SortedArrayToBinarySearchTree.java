package DS.BinaryTree.a10SortedArrayToBinarySearchTree;

import DS.BinaryTree.a01Traversal.TreeNode;

public class SortedArrayToBinarySearchTree {

    public TreeNode sortedArrayToBST(int[] nums) {
        return sortedArrayToBst(nums, 0, nums.length - 1);
    }

    private TreeNode sortedArrayToBst(int[] nums, int start, int end) {

        if (start > end)
            return null;

        int mid = start + (end - start) / 2;

        TreeNode root = new TreeNode();
        root.val = nums[mid];
        root.left = sortedArrayToBst(nums, start, mid - 1);
        root.right = sortedArrayToBst(nums, mid + 1, end);

        return root;
    }
}