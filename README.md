# Segment Tree with Lazy Propagation — Retail Sales Analytics

**Module:** COMP47500 — Advanced Data Structures in Java (2025/26 Spring)
**Author:** Jiawei Li (25212461)

## Overview

This project implements a **Segment Tree with lazy propagation** in plain
Java (no external dependencies) and applies it to a realistic scenario:
a retail chain's **daily sales analytics** engine. The chosen data
structure supports all four required operations in O(log n), which is the
motivating advantage over a naive array baseline.

The repository also ships with:

- a small **domain wrapper** (`SalesAnalytics`) that exposes the data
  structure as date-based retail operations,
- a **correctness test harness** (`SegmentTreeTest`) mixing hand-written
  edge cases and a randomised differential test,
- a **performance benchmark** (`PerformanceBenchmark`) that compares the
  segment tree against a naive O(n) array at multiple input sizes,
- a **narrative demo** (`Main`) that walks through the retail scenario
  end-to-end.

## What the Segment Tree supports

| Operation                            | Complexity |
| ------------------------------------ | ---------- |
| `new SegmentTree(long[] data)`       | O(n)       |
| `pointUpdate(i, v)`                  | O(log n)   |
| `rangeAdd(l, r, delta)`              | O(log n)   |
| `rangeSum(l, r)`                     | O(log n)   |
| `get(i)`                             | O(log n)   |

`rangeAdd` is the reason lazy propagation is included: without it a range
update would cost O((r − l) log n) when implemented as many point
updates. With lazy propagation, a fully-contained ancestor node records
a pending "add delta to every child" marker; the update is only pushed
down when a later query actually needs to descend through that node.
This keeps `rangeAdd` and `rangeSum` on the same O(log n) envelope.

## Retail Sales Analytics scenario

A retail chain records daily revenue across a fixed window (e.g. one
year). The analytics engine has to support:

- `getSales(day)` — the sales figure for a given day,
- `correctSales(day, newAmount)` — a POS correction for a past day,
- `totalRevenue(from, to)` — revenue over an inclusive date range,
- `applyBulkAdjustment(from, to, delta)` — e.g. a supplier rebate
  backdated across a range of days.

A naive array implementation would cost O(n) per range query or bulk
adjustment; the segment tree drops this to O(log n), which matters once
`n` grows from 365 days to tens of thousands of hourly / per-store
buckets.

## Project structure

```
GenAI_and_AdvDS_JiaweiLi_25212461/
├── README.md                  — this file
├── report.md                  — technical report scaffold
├── genai_log.md               — GenAI usage log
└── src/
    ├── SegmentTree.java          core data structure
    ├── SalesAnalytics.java       domain facade over SegmentTree
    ├── Main.java                 narrative demo (entry point)
    ├── SegmentTreeTest.java      correctness tests (entry point)
    └── PerformanceBenchmark.java benchmark harness (entry point)
```

There are no packages; every class lives in the default package. This
keeps `javac` / `java` invocations simple for the grader.

## Compile

From the project root:

```bash
javac -d out src/*.java
```

Compiled `.class` files land in `out/`.

## Run — demo

```bash
java -cp out Main
```

## Run — tests

```bash
java -cp out SegmentTreeTest
```

Output ends with a summary line of the form:

```
=== SegmentTreeTest summary ===
  passed: 1896
  failed: 0
```

Exit code `0` means all tests passed. The randomised differential section
is deliberately silent on success; individual failures (if any) are still
printed.

## Run — benchmark

Default multi-size sweep (recommended for assignment evidence):

```bash
java -cp out PerformanceBenchmark
```

This runs at `n = 1_000, 10_000, 50_000, 100_000` with `numOps == n` and
prints a table plus an interpretation line. A representative observed
average speed-up is around **12x**; the exact number depends on JVM,
hardware and JIT warmth.

Custom single run:

```bash
java -cp out PerformanceBenchmark 200000 100000 42
# arguments: n numOps seed
```

Checksum mismatches between the naive and segment-tree runs cause the
benchmark to exit with code `1`.

## Complexity summary

| Approach                  | Point update | Range sum        | Range add      |
| ------------------------- | ------------ | ---------------- | -------------- |
| Naive `long[]` baseline   | O(1)         | O(r − l)         | O(r − l)       |
| **Segment tree (this)**   | O(log n)     | **O(log n)**     | **O(log n)**   |

Space: O(n) — the tree is stored in an array of length `4n`, which is a
standard safe upper bound regardless of whether `n` is a power of two.

## Design notes

- **Empty input is rejected explicitly** by the `SegmentTree`
  constructor (`IllegalArgumentException`). Every public operation is
  index-based and nothing meaningful can be asked of an empty tree, so
  failing fast at construction is clearer than returning a tree that
  would throw on every subsequent call.
- **Monetary values are stored as `long` cents**, never `double`. This
  keeps the randomised differential test deterministic.
- **`SalesAnalytics` is deliberately a thin facade** over `SegmentTree`.
  The core data structure has no retail-specific concepts so it stays
  reusable for other problems (temperature sensors, leaderboards, ...).

## Notes

Built for COMP47500 Advanced Data Structures in Java, 2025/26 Spring.
Plain Java only — no Maven, no Gradle, no external libraries.
