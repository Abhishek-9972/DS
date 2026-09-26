````
DAU
= 100M

Actions / user / day
= 20

Requests / day
= 100M × 20
= 2B requests/day

Average QPS
= 2B / 86,400
≈ 24K QPS

Peak factor
= 5×

Peak QPS
= 24K × 5
= 120K QPS

Read / Write
= 90% / 10%

Peak Read QPS
= 120K × 90%
= 108K QPS

Peak Write QPS
= 120K × 10%
= 12K QPS
````
## Capacity Estimation

```
100M DAU
    ↓
20 actions/user/day
    ↓
2B requests/day
    ↓
24K average QPS
    ↓
5× peak
    ↓
120K peak QPS
    ↓
108K reads
12K writes
    ↓
50M content/day
    ↓
100 GB/day metadata
    ↓
5–10 TB/day media
```

# Storage

## Metadata

````
New content
= 50M/day

Metadata per content
≈ 2 KB

Storage/day
= 50M × 2 KB
≈ 100 GB/day

Storage/year
≈ 100 GB × 365
≈ 36.5 TB/year
````


## Media

````
Media content/day
= 50M × 60%
= 30M/day

Average media size
≈ 500 KB

Media storage/day
= 30M × 500 KB
≈ 15 TB/day

Media storage/year
≈ 15 TB × 365
≈ 5.5 PB/year
````


## What to remember

````
Logical storage:
Metadata → 100 GB/day
Media    → 15 TB/day

Physical storage with 3× replication:
Metadata → 300 GB/day
Media    → 45 TB/day
````
