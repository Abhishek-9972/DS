Yes. Before capacity estimation, get these **order-of-magnitude conversions completely automatic**. You don't need to memorize every possible number—just the key anchors.

## 1. Million → Billion → Trillion

The most important pattern:

```text
1 Thousand   = 1,000          = 10³
1 Million    = 1,000,000      = 10⁶
1 Billion    = 1,000,000,000  = 10⁹
1 Trillion   = 1,000,000,000,000 = 10¹²
```

Think in groups of **3 zeros**:

```text
Thousand → Million → Billion → Trillion
   ×1000     ×1000      ×1000
```

### Numbers worth knowing

```text
1M     = 1,000,000
10M    = 10,000,000
100M   = 100,000,000

1B     = 1,000M
10B    = 10,000M
100B   = 100,000M

1T     = 1,000B
```

### Indian numbering

Since you're interviewing in India, be comfortable with this too:

```text
1 Million  = 10 lakh
10 Million = 1 crore
100 Million = 10 crore
1 Billion  = 100 crore
10 Billion = 1,000 crore
1 Trillion = 1 lakh crore
```

So:

> **1 Billion = 100 Crore**

This is especially useful when converting large system numbers mentally.

---

# 2. KB → MB → GB → TB → PB

For system-design interviews, use the **decimal convention**:

```text
1 KB = 1,000 bytes
1 MB = 1,000 KB
1 GB = 1,000 MB
1 TB = 1,000 GB
1 PB = 1,000 TB
```

So:

```text
KB → MB → GB → TB → PB
 ×1000 each step
```

### The key anchors

```text
1 MB = 1,000 KB
1 GB = 1,000 MB
1 TB = 1,000 GB
1 PB = 1,000 TB
```

And therefore:

```text
1 GB = 1,000,000 KB
1 TB = 1,000,000 MB
1 PB = 1,000,000 GB
```

---

# 3. Don't confuse bits and bytes

This is **very important** for bandwidth.

```text
b = bit
B = Byte

1 Byte = 8 bits
```

Therefore:

```text
1 MB/s = 8 Mbps
1 GB/s = 8 Gbps
```

Example:

```text
800 Mbps
÷ 8
= 100 MB/s
```

### Remember

> **Capital B = Byte**
> **Small b = bit**

---

# 4. Time conversions

These are extremely useful for QPS calculations.

```text
1 minute = 60 seconds
1 hour   = 60 minutes
1 day    = 24 hours

1 day = 86,400 seconds
```

For interview mental math:

```text
1 day ≈ 100,000 seconds
```

This gives you a very easy approximation.

For example:

```text
1 Billion requests/day
≈ 1,000,000,000 / 100,000
≈ 10,000 QPS
```

Actual:

```text
1B / 86,400 ≈ 11.6K QPS
```

So we remember:

> **1B/day ≈ 12K QPS**

---

# 5. QPS shortcuts

This is where all those numbers come together.

### Memorize this one:

> **1 Billion requests/day ≈ 12K QPS**

Then:

```text
100M/day ≈ 1.2K QPS
1B/day   ≈ 12K QPS
2B/day   ≈ 24K QPS
10B/day  ≈ 120K QPS
```

You don't need to calculate 86,400 every time.

---

# 6. Storage multiplication

If you have:

```text
10M records
× 1 KB each
```

Then:

```text
10M KB
= 10,000 GB
= 10 TB
```

A very useful shortcut:

### 1M objects

```text
1 KB each → 1 GB
10 KB each → 10 GB
100 KB each → 100 GB
1 MB each → 1 TB
```

This is worth memorizing.

---

# 7. Replication

If your logical data is:

```text
10 TB
```

and replication factor is 3:

```text
10 TB × 3
= 30 TB physical storage
```

So:

```text
Logical storage × replication factor
= physical storage
```

Don't automatically assume replication = 3 in every system. State it as an assumption.

---

# 8. Percentages

You'll constantly use these:

```text
50% = 1/2
25% = 1/4
20% = 1/5
10% = 1/10
5%  = 1/20
1%  = 1/100
```

Example:

```text
120K QPS
× 90%
≈ 108K reads

120K QPS
× 10%
≈ 12K writes
```

---

# 9. The hierarchy I want you to memorize

This is your **Capacity Estimation foundation**:

```text
NUMBERS

1K = 1,000
1M = 1,000K
1B = 1,000M
1T = 1,000B


STORAGE

1 KB = 1,000 bytes
1 MB = 1,000 KB
1 GB = 1,000 MB
1 TB = 1,000 GB
1 PB = 1,000 TB


NETWORK

1 Byte = 8 bits
1 GB/s = 8 Gbps


TIME

1 min = 60 sec
1 hour = 3,600 sec
1 day = 86,400 sec


QPS

1B/day ≈ 12K QPS


REPLICATION

Physical storage
= Logical storage × replication factor
```

### And one mental picture

```text
              NUMBERS
                 │
      ┌──────────┴──────────┐
      ↓                     ↓
   Million                 Storage
      ↓                     ↓
   Billion        KB → MB → GB → TB → PB
      ↓
   Trillion
      │
      └──────→ QPS ←────── Time
```

If these are automatic, **capacity estimation becomes mostly multiplication/division and architecture reasoning**, rather than a math exercise.
