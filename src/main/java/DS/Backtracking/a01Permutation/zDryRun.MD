# Dry Run

### Input

```text
nums = [1,2,3]
```

Initial State

```text
partial = []
used    = [F,F,F]
result  = []
```

---

### Step 1

Choose **1**

```text
partial = [1]
used    = [T,F,F]
```

Choose **2**

```text
partial = [1,2]
used    = [T,T,F]
```

Choose **3**

```text
partial = [1,2,3]
used    = [T,T,T]
```

All elements are used.

```text
result = [[1,2,3]]
```

Backtrack

```text
Remove 3

partial = [1,2]
used    = [T,T,F]
```

---

### Step 2

From `[1]`, choose **3**

```text
partial = [1,3]
used    = [T,F,T]
```

Choose **2**

```text
partial = [1,3,2]
```

Add to result

```text
result =
[
    [1,2,3],
    [1,3,2]
]
```

Backtrack completely to

```text
partial = []
used    = [F,F,F]
```

---

### Step 3

Choose **2**

Generate

```text
[2,1,3]
[2,3,1]
```

---

### Step 4

Choose **3**

Generate

```text
[3,1,2]
[3,2,1]
```

---

## Final Output

```text
[
    [1,2,3],
    [1,3,2],
    [2,1,3],
    [2,3,1],
    [3,1,2],
    [3,2,1]
]
```

---

# Core Idea

At every recursive call:

1. Pick one unused element.
2. Mark it as used.
3. Add it to the current permutation (`partial`).
4. Recurse to build the remaining permutation.
5. Undo the choice (Backtrack) by:
    - Removing the last element from `partial`
    - Marking it as unused

Backtracking ensures every possible permutation is explored.

---

# Time Complexity

- **Time:** `O(n × n!)`
- **Space:** `O(n)` (excluding output)