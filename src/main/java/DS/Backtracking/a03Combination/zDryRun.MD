# Dry Run

### Input

```text
n = 4
k = 2
```

Initial State

```text
start   = 1
partial = []
result  = []
```

---

### Step 1

Choose **1**

```text
partial = [1]
```

Recursive call

```text
start = 2
```

Choose **2**

```text
partial = [1,2]
```

Size == k

```text
Add [1,2]
```

Backtrack

```text
partial = [1]
```

Choose **3**

```text
partial = [1,3]
```

Add

```text
[1,3]
```

Backtrack

Choose **4**

```text
partial = [1,4]
```

Add

```text
[1,4]
```

Backtrack to

```text
partial = []
```

---

### Step 2

Choose **2**

```text
partial = [2]
start   = 3
```

Choose **3**

```text
partial = [2,3]
```

Add

```text
[2,3]
```

Backtrack

Choose **4**

```text
partial = [2,4]
```

Add

```text
[2,4]
```

Backtrack to

```text
partial = []
```

---

### Step 3

Choose **3**

```text
partial = [3]
start   = 4
```

Choose **4**

```text
partial = [3,4]
```

Add

```text
[3,4]
```

Backtrack

---

### Step 4

Choose **4**

```text
partial = [4]
```

No more numbers are available to make a combination of size 2.

Backtrack.

---

## Final Output

```text
[
    [1,2],
    [1,3],
    [1,4],
    [2,3],
    [2,4],
    [3,4]
]
```

---

# Core Idea

- Start from the current number (`start`).
- Choose the current number and add it to the combination.
- Recurse with `start = i + 1` so that the same number is never chosen again.
- When the combination size becomes `k`, add it to the result.
- Backtrack by removing the last chosen number and continue exploring.

---

# Why `i + 1`?

Using `i + 1` ensures:

- Each number is used only once.
- Duplicate combinations like `[2,1]` are never generated after `[1,2]`.
- We generate **combinations**, not **permutations**.

---

# Time Complexity

- **Time:** `O(C(n, k) × k)`
- **Space:** `O(k)` (excluding output)