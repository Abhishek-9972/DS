# Dry Run - Subarray Sum Equals K

## Problem

Given:

```java
nums = [1, 2, 3]
k = 3
```

Find the number of continuous subarrays whose sum equals `k`.

---

## Initial State

```text
map = {0=1}
prefixSum = 0
count = 0
```

Why do we put `{0=1}`?

Because if the prefix sum itself becomes equal to `k`, then:

```
prefixSum - k = 0
```

and we want to count that subarray starting from index `0`.

---

## Iteration 1

### Current Number

```
num = 1
```

### Update Prefix Sum

```
prefixSum = 0 + 1 = 1
```

### Check if a previous prefix sum exists

```
prefixSum - k
= 1 - 3
= -2
```

```
map.get(-2) = 0
```

So,

```
count = 0
```

### Store current prefix sum

```
map = {
    0=1,
    1=1
}
```

---

## Iteration 2

### Current Number

```
num = 2
```

### Update Prefix Sum

```
prefixSum = 1 + 2 = 3
```

### Check

```
prefixSum - k
= 3 - 3
= 0
```

```
map.get(0) = 1
```

Meaning:

There is one previous prefix sum that allows a subarray sum of `3`.

Increase count.

```
count = 1
```

The subarray is:

```
[1,2]
```

### Store current prefix sum

```
map = {
    0=1,
    1=1,
    3=1
}
```

---

## Iteration 3

### Current Number

```
num = 3
```

### Update Prefix Sum

```
prefixSum = 3 + 3 = 6
```

### Check

```
prefixSum - k
= 6 - 3
= 3
```

```
map.get(3) = 1
```

So,

```
count = 2
```

The matching subarray is:

```
[3]
```

### Store current prefix sum

```
map = {
    0=1,
    1=1,
    3=1,
    6=1
}
```

---

# Final Answer

```
count = 2
```

Matching subarrays:

```
[1,2]
[3]
```

---

# How the Formula Works

Suppose at some index,

```
Current Prefix Sum = P
```

We want a subarray whose sum is `k`.

Assume an earlier prefix sum was `X`.

```
Subarray Sum
= Current Prefix Sum - Earlier Prefix Sum

k = P - X

X = P - k
```

So every time we compute a new prefix sum, we simply ask:

> "Have I seen a prefix sum equal to `prefixSum - k` before?"

If yes,

every occurrence represents one valid subarray ending at the current index.

---

# Why Store Frequency Instead of Just Presence?

Consider:

```
nums = [1, -1, 1, -1, 1]
k = 1
```

The same prefix sum appears multiple times.

```
Prefix Sums

Index    Prefix Sum
-------------------
-1          0
0           1
1           0
2           1
3           0
4           1
```

Here,

```
Prefix Sum 0 appears 3 times.
Prefix Sum 1 appears 3 times.
```

Each occurrence can create a different valid subarray.

Therefore we store:

```
Prefix Sum -> Frequency
```

instead of

```
Prefix Sum -> Exists
```

---

# Time Complexity

```
O(n)
```

Each element is visited once.

---

# Space Complexity

```
O(n)
```

In the worst case, every prefix sum is unique and stored in the HashMap.