# ARRAY

1) Two Sum

```
Map<Integer,Integer>   
map.put(nums[i],i);
map.containsKey(target-nums[i])
```

2) ContainerWithMost Water

```
while(aPointer<bPointer)
if(h[aPointer] < h[bPointer])
max = Math.max(h[aPointer] * bPointer-aPointer)
```

3) TrappingRainWater

````
LeftMax - Math.max(height[i],leftMax[i-1])
RightMax - Math.max(height[i], rightMax[i+1])
result[i] = Math.min(LeftMax,RightMax) - height[i]
result = sum of result[i]
````

4) ThreeSum

````
Arrays.sort(arr)
for(int i=0; i<n-2;i++)
-> l = i+1, r= n-1
   while(l<r){
   if(sum==0)
   {l++;r--;res.add(Sum)}
   else{sum<0){l++}
   else{r--}
}
````

5) ThreeSumClosest

````
Arrays.sort(arr)
closesSum = sum of firstThree

if(Math.abs(target-sum)<Math.abs(closestSum-target))
{closestSum = sum;
````

6) FourSum

````
Arrays.sort(arr)
for(int i=0; i<n-2;i++)
for(int j=i+1; j<n; j++)
-> l = j+1, r= n-1
   while(l<r){
   if(sum==0)
   {l++;r--;res.add(Sum)}
   else{sum<0){l++}
   else{r--}
}
````

7) Remove Duplicates From Sorted Array

````
for(i=0 -> n-1)
{ 
   while(i+1<n && nums[i]==nums[i+1])
   {i++;}
   res[counter] = arr[i];
   counter++;
}
````

8) SearchInRotatedSortedArray

````
while(start<=end)
{
if(ele == arr[mid]) -> return mid;
else if(arr[st]<=arr[mid])
{
   if(arr[st]>=ele && ele<=arr[end])
      {
         end = mid-1;
      }
   else{
      start = mid+1;
      }     
}
else{
- - -
````

9) FirstAndLastPositionInSortedArray

````
first = mid
end = mid-1

last = mid
start = mid+1
````

10) SearchInsertPosition

````
return start
````

11) MaxSubArray

````
sum<0 = sum = 0

max = Math.max(max, sum + nums[i])
````

12) Jump1

````
for(int i=0; i<n; i++)
if(reachable<0) {return false;}
reachable = Math.max(reachable, i+nums[i])
````

13) Stock1

````
for(int i=0; i<n; i++)
if(prices[i]<min) {min = prices[i]}
else {max = Math.max(max, prices[i]-min}}
````

14) Stock2

````
for(int i=0; i<n-1; i++)
if(price[i]<price[i+1]{
sum = sum + (price[i+1]-price[i])
````

16) SingleNumber

````
result = result ^ nums[i]
````

17) MaximumProductSubArray

````
for(i=0->n)
{
l = l==0? 1 : l
r = r==0? 1 : r

l = l*nums[i]
r = r*nums[n-1-i]

max = Math.max(max, Math.max(l,r))
````

18) MinimumInSortedArray

````
while(st<=end)
{
    if(mid<=end)
    {
    end = mid;
    }
    else{
    st = mid+1;
    }
}
return st;
````

19) Rotate Sorted Array

````
    rotate(arr, st, end){
    while(st<end)
    temp = arr[st]
    arr[st] = arr[end]
    arr[end] = arr[st]
    st++
    end--
    }
   
    rotate(arr, 0, n-1)
    rotate(arr, 0, k-1)
    rotate(arr, k, n-1)   
````

20) MinimumSubArrayLength

````
    for(i=0 -> n)
    sum = sum+nums[i]
    if(sum>=target)
    min = Math.min(min, i+1-left)
    sum = sum - nums[left]
    left++
````

21) MajorityElement1

````
    for(int num:nums)
    if(count == 0){
        ele = num
    }
    
    if(num==ele) -> count++;
    else -> count--;
    
    return ele
````

22) GroupAnagrams

````
    Map<String, List<String>> anagrams
    Arrays.sort(str.toCharArray(s))
````

23) RemoveDuplicatesFromSortedArray02

````
    for(int i=0; i<n; i++)
    int start = nums[i]
    while(i+1<n && nums[i]==nums[i+1]) -> i++
    
    if(nums[i] != st) -> nums[c++] = nums[i] nums[c++] = nums[i]
    else nums[c++] = nums[i]
````

24) MergeSortedArray

````
    Add both arr to a new arr
    Sort the new arr
````

29) TopKFrequentElements

````
    PriorityQueue<Map.Entry<Integer,Integer>> pq = new PriorityQueue<>((a,b) -> b.getValue() - a.getValue())
    pq.put(m1)
    if(pq.size()>k) -> pq.poll()
````

30) InsertDeleteRandomO1

````
    List Map
    
    search(val)
    map.containsKey(val)
    
    insert(val)
    if(map.containsKey(val)) -> return false
    else
        list.add(val)
        map.put(val, list.size()-1)
        
    remove(val)
    if(!map.containsKey(val)) -> return false
    else{
        int index = map.get(val)
        list.set(index, list.get(list.size()-1)
        map.put(list.get(index), index)
        map.remove(val)
        list.remove(list.size()-1)
````

37) ReverseArrayInGroups

````
    i=0, j = k-1
    while(i<n)
    reverse(arr, i, j)
    i = i+k
    j = j+k
````

38) ElementsGreaterThanLeftAndSmallerThanRight

````
    left[0] = Integer.MIN_VALUE
    leftMax
    right[n-1] = Integer.MAX_VALUE
    RightMin
    for((i=0->n)
    if(ele > min && ele<max) -> return i;  
````

40) KthClosestPointsToTheArray

````
PQ<int[]> pq = new PQ<>((a,b) -> (b[0]*b[0] + b[1]*b[1])
                        - (a[0]*a[0] + a[1]*a[1])
                        
                        for(int[] point : points)
                        pq.add(point)
                        
                        if(pq.size()>k)
                        {pq.poll()){
````

41) FirstUniqueCharacterInString

````
    if(map.containsKey(char))
    map.put(char, i)
    else
    map.put(char, -1)
````    

# BACKTRACKING

8) LetterCombinationsOfPhoneNumber

````
    String word = map.get(s.charAt(idx))
    for(char ch : word.toCharArray())
````

9) GenerateParentheses

````
    generate(left, right, s, n, res)
    if(s.length== n*2) -> res.add(s))
    if(left<n) -> generate(left+1, right, s+"("," res)
    if(right<left) -> generate(left, right+1, s+")", res)
````

# BINARY SEARCH

7) PeakElement

````
    arr[0] > arr[1] -> return arr[0]
    arr[n-1] > arr[n-2] -> return arr[n-1]
    
    int start = 1; int end = n-2
    while(st<=end)
    if(arr[mid]>arr[mid+1] && arr[mid]>arr[mid-1]){return mid}
    else if(arr[mid]>arr[mid+1]) -> end = mid-1;
    else -> st = mid+1
````

8) CeilOfAnElement

````    
    end = mid-1
    res = arr[mid]
````

8) FloorOfAnElement

````    
    start = mid+1
    res = arr[mid]
````

12) TimeBasedKeyValueStore

````
    Map<String, List<Data>>
    
    get(key, time)
    
    map.get(key) => List<Data>
    
    while(st<=end)
    {
        if(mid.time == time) -> return mid.val
        else if(mid.time<time)
        start = mid+1
        res = mid.val
        
    }
````

# BINARY TREE

1) Traversal

````
    preOrder(TreeNode node)
    if(node!=null)
    sout(node.data)
    preOrder(node.left)
    preOrder(node.right)
````

3) ValidateBST

````
    isValidBST(TreeNode node, int min, int max)
    i(node==null) -> return true;
    if(node.left<=min && node.rigth>=max) -> return false;
    return isValidBST(node, min, root.data) && isValidBST(node, root.data, max)
````

4) SameTree

````
    isSameTree(TreeNode p, TreeNode q)
    if(p==null && q==null) return true
    if(p==null || q==null) return false
    return p.data==q.data && isSameTree(p.left, q.left) && isSameTree(p.right, q.right)
````

5) Symmetric Tree

````
    isSameTree(TreeNode p, TreeNode q)
    if(p==null && q==null) return true
    if(p==null || q==null) return false
    return p.data==q.data && isSameTree(p.left, q.right) && isSameTree(p.left, q.right)
````

6) LevelOrderTraversal

````
    Queue<TreeNode> queue = new Queue<>();
    queue.add(root)
    while(!queue.isEmpty()){
        int size = queue.size()
        for(int i=0; i<size; i++){
        TreeNode node = queue.poll()
        sout(node.data)
        if(node.left!=null) -> queue.add(node.left)
        if(node.right!=null) -> queue.add(node.right)
    }
````

7) ZigZagLevelOrderTraversal

````
    Queue<TreeNode> queue = new Queue<>();
    queue.add(root)
    int level = 0
    while(!queue.isEmpty()){
        level++
        int size = queue.size()
        for(int i=0; i<size; i++){
        TreeNode node = queue.poll()
        if(level%2!=0) -> smallAns.add(node.data)
        else smallAns.addFirst(node.data)
        if(node.left!=null) -> queue.add(node.left)
        if(node.right!=null) -> queue.add(node.right)
    }
````

8) MaximumDepthOfBinaryTree

````
    maxDepth(TreeNode root)
    if(root==null) return 0
    int left = maxDepth(root.left)
    int right = maxDepth(root.right)
    return 1 + Math.max(left,right)
````

9) BinaryTreeFromPreOrderInOrder

````
    preOrderInOrder(int[] preOrder, int[] inOrder, int preStart, int inStart, int inEnd)
        if(preStart>preOrder.length-1 && inStart>inEnd) -> return null
        
        TreeNode root = new TreeNode(preOrder[preStart])
        
        for(int i=inStart; i<=inEnd; i++)
        if(root.val == inOrder[i]) -> inIndex = i
        
        root.left = preOrderInOrder(preStart+1, instart, inIndex-1)
        root.right = preOrderInOrder(preStart+inIndex-instart+1, inIndex+1, inEnd)
````

10) BinaryTreeFromInOrderPostOrder

````
    postOrderInOrder(int[] postOrder, int[] inOrder, int postStart, int postEnd, int inStart, int inEnd)
        if(preStart>preOrder.length-1 && inStart>inEnd) -> return null
        
        TreeNode root = new TreeNode(postOrder[postEnd-1])
        
        for(int i=inStart; i<=inEnd; i++)
        if(root.val == inOrder[i]) -> inIndex = i
        
        int left = inIndex - inStart
        int right = inEnd - inIndex
        
        root.left = preOrderInOrder(postStart, postStart + left - 1, instart, inIndex-1)
        root.right = preOrderInOrder(postEnd - right, postEnd-1, inIndex+1, inEnd)
````

11) SortedArrayToBST

````
    TreeNode sortedArrayToBST(arr, start, end)
        if(start>end) -> return null
        int mid = start + end/2;
        TreeNode root = new TreeNode(mid);
        root.left = sortedArrayToBST(arr, start, mid-1)
        root.right = sortedArrayToBST(arr, mid+1, end)
````

12) BalancedBinaryTree

````
    isBalancedBinaryTree(TreeNode root)
    int diff = Math.abs(height(root.left) - height(root.right))
    if(diff>1) -> return false
    return isBalancedBinaryTree(root.left) && isBalancedBinaryTree(root.right)
````

13) MinimumBinaryTree

````
    minimum(TreeNode root)
        if(root==null) return null
        int left = min(root.left)
        int right = min(root.right)
        if(left == 0 || right ==0)
            return 1+ max(left, right)
        else return 1 + min(left, right)
````

14) PathSum

````
    pathSum(TreeNode root, int remain)
        if(remain<0) return
        if(root.left == null && root.right == null)
             return root.val == remain
        boolean left = pathSum(root.left, remain - root.val)
        boolean right = pathSum(root.right, remain - root.val)
        return left || right     
````

17) PathSum2

````
    pathSum2(TreeNode root, int target, int currSum, List<Integer> curList)
        if(root == null) return
        curList.add(root.val)
        if(root.left == null && root.right ==null && currSum + root.val == target)
            res.add(curList)
        else
            pathSum2(root.left, target, currSum + root.val, curList)
            pathSum2(root.right, target, currSum + root.val, curList)
        curList.remove(curList.size()-1)

````

17) SumRootToLeafNumbers

````
    pathSum(TreeNode root, String path, List<String> paths)
        if(root==null)
            return paths
        path+=root.val
        if(root.left==null && root.right == null)
            paths.add(path)
            return
        if(root.left==null)
            pathSum(root.left, path, paths)
        if(root.right == null)
            pathSum(root.right, path, paths)     
````

18) BinaryTreeRightSideView