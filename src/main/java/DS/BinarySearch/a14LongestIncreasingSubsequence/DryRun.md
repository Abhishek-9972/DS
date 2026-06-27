# Dry Run - Longest Increasing Subsequence (Binary Search)

**Input**

```text
nums = [10,9,2,5,3,7,101,18]
```

Initially

```text
tails = []
size = 0
```

| Number | Action                     | tails       | size |
| ------ | -------------------------- | ----------- | ---- |
| 10     | Append (tails is empty)    | [10]        | 1    |
| 9      | Replace first element ≥ 9  | [9]         | 1    |
| 2      | Replace first element ≥ 2  | [2]         | 1    |
| 5      | Append (5 > 2)             | [2,5]       | 2    |
| 3      | Replace first element ≥ 3  | [2,3]       | 2    |
| 7      | Append (7 > 3)             | [2,3,7]     | 3    |
| 101    | Append (101 > 7)           | [2,3,7,101] | 4    |
| 18     | Replace first element ≥ 18 | [2,3,7,18]  | 4    |

**Final Answer**

```text
Length of LIS = 4
```

### Key Observation

* `tails[i]` stores the **smallest possible tail** of an increasing subsequence of length `i + 1`.
* `tails` does **not** represent the actual LIS.
* Replacing a larger tail with a smaller one increases the chances of extending the subsequence later.

For this example:

```text
Actual LIS = [2,3,7,101]
```

but the final `tails` array becomes

```text
[2,3,7,18]
```

The values are different, but the **length (4)** is the same, which is the required answer.
