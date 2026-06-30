A Heap is a special Tree-based data structure in which the tree is a complete binary tree. Generally, Heaps can be of two types:

**Max-Heap**: In a Max-Heap the key present at the root node must be greatest among the keys present at all of it’s children. The same property must be recursively true for all sub-trees in that Binary Tree.

**Min-Heap**: In a Min-Heap the key present at the root node must be minimum among the keys present at all of it’s children. The same property must be recursively true for all sub-trees in that Binary Tree.

PriorityQueue.remove() (without any argument) always removes the root of the heap.

Since Java's default PriorityQueue is a Min Heap, the root is the smallest element.