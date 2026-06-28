# Dry Run - Jump Game II (Greedy)

**Input**

```text
nums = [2,3,1,1,4]
```

Initially

```text
jumps = 0
currentEnd = 0
farthest = 0
```

| i | nums[i] | farthest = max(farthest, i + nums[i]) | currentEnd | jumps | Action                                                   |
| - | ------- | ------------------------------------- | ---------- | ----- | -------------------------------------------------------- |
| 0 | 2       | max(0, 0+2) = **2**                   | 0          | 0     | `i == currentEnd` → Jump → `jumps = 1`, `currentEnd = 2` |
| 1 | 3       | max(2, 1+3) = **4**                   | 2          | 1     | Continue exploring current range                         |
| 2 | 1       | max(4, 2+1) = **4**                   | 2          | 1     | `i == currentEnd` → Jump → `jumps = 2`, `currentEnd = 4` |
| 3 | 1       | Not processed (`i < nums.length - 1`) | -          | -     | Destination already reachable                            |

**Final Answer**

```text
Minimum Jumps = 2
```

### Intuition

* `currentEnd` represents the farthest index we can reach using the current number of jumps.
* `farthest` stores the farthest index reachable while exploring the current range.
* Whenever `i == currentEnd`, we have explored the entire current range, so we must take another jump and update `currentEnd = farthest`.
