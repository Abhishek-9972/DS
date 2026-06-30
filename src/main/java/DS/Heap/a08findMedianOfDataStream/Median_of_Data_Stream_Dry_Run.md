# Median of Data Stream - Dry Run

See content below.

## Input Stream

``` text
5, 15, 1, 3, 8, 7
```

Remember: - **Max Heap** stores the smaller half. - **Min Heap** stores
the larger half. - Max Heap is allowed one extra element.

  Inserted   Max Heap    Min Heap     Median
  ---------- ----------- ------------ --------
  5          \[5\]       \[\]         5
  15         \[5\]       \[15\]       10
  1          \[5,1\]     \[15\]       5
  3          \[3,1\]     \[5,15\]     4
  8          \[5,3,1\]   \[8,15\]     5
  7          \[5,3,1\]   \[7,8,15\]   6

## Key Insight

``` text
Smaller Half            Larger Half
(Max Heap)              (Min Heap)

5 3 1      |      7 8 15
```

-   Odd count → Max Heap root is the median.
-   Even count → Average of both heap roots is the median.
