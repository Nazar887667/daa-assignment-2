# Assignment 2 — Algorithmic Analysis, Correctness and Performance Trade-offs

## 1. Overview

This project implements and analyzes three data structures in Java:

- **Dynamic Array** (`src/DynamicArray.java`) — a resizable array backed by
  `Object[]`, doubling capacity on overflow (the same strategy `ArrayList` uses).
- **Linked List** (`src/LinkedList.java`) — a singly linked list with head *and*
  tail pointers, so appends are O(1).
- **Min-Heap** (`src/MinHeap.java`) — a binary min-heap stored in a dynamic array,
  the standard "complete binary tree in array form" layout.

The purpose of the assignment is not just to implement these structures, but to
**prove they are correct** (loop invariants), **analyze their complexity**
(O/Ω/Θ), and then **measure their real performance** under four controlled
workloads, comparing theory against experiment.

Every one of the required operations — `add(x)`, `add(index,x)`, `remove(index)`,
`get(index)`, `contains(x)` for Dynamic Array and Linked List, and `insert(x)`,
`peekMin()`, `extractMin()` for Min-Heap — is implemented from scratch (no
`java.util.ArrayList`/`LinkedList`/`PriorityQueue` internals are used in the
implementations themselves; `java.util` collections are used only in `Tests.java`
as an independent reference to cross-check correctness, exactly as the
assignment permits).

## 2. Complexity Analysis

### 2.1 Dynamic Array

| Operation      | Best     | Average  | Worst    | Aux. space |
|-----------------|----------|----------|----------|------------|
| `add(x)`         | Θ(1)     | Θ(1)     | O(n)     | O(1) amortized, O(n) on resize |
| `add(index,x)`   | Ω(1)     | Θ(n)     | O(n)     | O(1) (O(n) if it also resizes) |
| `remove(index)`  | Ω(1)     | Θ(n)     | O(n)     | O(1) |
| `get(index)`     | Θ(1)     | Θ(1)     | Θ(1)     | O(1) |
| `contains(x)`    | Ω(1)     | Θ(n)     | O(n)     | O(1) |

Justification:
- `add(x)` appends at the end. Normally this is a single array write, Θ(1).
  When the backing array is full, a resize copies all n elements, giving the
  O(n) worst case; because doubling happens only every ~n insertions, the
  **amortized** cost over a sequence of n appends is still Θ(1) per call.
- `add(index,x)` and `remove(index)` must shift every element from `index` to
  the end by one slot. Best case is `index == size` (append, no shifting,
  Ω(1)); worst case is `index == 0` (shift everything, O(n)); a uniformly
  random index gives Θ(n) average, since on average half the array is shifted.
- `get(index)` computes a direct memory offset — always Θ(1), regardless of
  where the element is.
- `contains(x)` must scan linearly; best case the target is at position 0
  (Ω(1)), worst case it is absent or at the last position (O(n)).

### 2.2 Linked List (singly linked, head + tail)

| Operation      | Best     | Average  | Worst    | Aux. space |
|-----------------|----------|----------|----------|------------|
| `add(x)`         | Θ(1)     | Θ(1)     | Θ(1)     | O(1) |
| `add(index,x)`   | Ω(1)     | Θ(n)     | O(n)     | O(1) |
| `remove(index)`  | Ω(1)     | Θ(n)     | O(n)     | O(1) |
| `get(index)`     | Ω(1)     | Θ(n)     | O(n)     | O(1) |
| `contains(x)`    | Ω(1)     | Θ(n)     | O(n)     | O(1) |

Justification:
- `add(x)` appends at the tail, which is a known pointer — always Θ(1),
  unlike a naive singly linked list without a tail pointer, which would be O(n).
- `add(index,x)` / `remove(index)` are Ω(1) only for `index == 0` (and, thanks
  to the tail pointer, `add` is also O(1) for `index == size`); any other
  index requires walking from the head, giving Θ(n) on average and O(n) worst
  case (`index == size-1` for removal, since there is no backward pointer to
  the predecessor of the tail).
- `get(index)` and `contains(x)` have **no random access at all** — every
  lookup, even for `index == 0`, still requires walking pointers from the
  head, so the *only* way to get Ω(1) is when the very first node is what you
  want (`index == 0`, or the target is the head's value for `contains`).

### 2.3 Min-Heap (array-backed binary heap)

| Operation      | Best     | Average  | Worst    | Aux. space |
|-----------------|----------|----------|----------|------------|
| `insert(x)`      | Ω(1)     | Θ(log n) | O(log n) | O(1) amortized (O(n) on resize) |
| `peekMin()`      | Θ(1)     | Θ(1)     | Θ(1)     | O(1) |
| `extractMin()`   | Ω(1)     | Θ(log n) | O(log n) | O(1) |

Justification:
- `insert(x)` appends the new element as the last leaf, then sifts it up
  while it is smaller than its parent. Best case: the new element is already
  ≥ its parent, so the sift-up loop runs 0 times, Ω(1). Worst/average case:
  it must travel up to the root, which is at most ⌊log₂ n⌋ levels away,
  giving O(log n)/Θ(log n) comparisons.
- `peekMin()` simply reads index 0 — always Θ(1).
- `extractMin()` swaps the root with the last leaf, shrinks the heap, then
  sifts the new root down. Best case: the heap becomes empty or has one
  element left (Ω(1)); worst/average case: it sifts down the full height of
  the tree, O(log n)/Θ(log n) comparisons (two comparisons per level: left
  child and right child).

### 2.4 Operations that look similar but differ in practice

`get(index)` is Θ(1) for Dynamic Array but Θ(n) for Linked List even though
both "just retrieve an element" — the difference is that an array computes an
address directly, while a list must traverse pointers, so **"look up
position i" is not the same cost primitive on the two structures.**
Likewise, `add(x)` is Θ(1) on both structures, but for very different
reasons: the array's Θ(1) is *amortized* (occasional O(n) resize hidden
inside it), while the list's Θ(1) is a true worst-case constant (no resizing
needed, ever) — this shows up in Workload 3 (Section 6) as noisier array
timings even when both structures are asymptotically equal.

## 3. Correctness — Loop Invariant Proofs

Two operations are proved correct below. Per the requirement, at least one
comes from a loop-based operation; both of the two chosen here are.

### 3.1 `DynamicArray.add(index, x)` — right-to-left shifting loop

```java
for (int i = size - 1; i >= index; i--) {
    data[i + 1] = data[i];
    opCount++;
}
data[index] = x;
size++;
```

**Loop invariant:** *At the start of each iteration of the loop (for the
current value of `i`), every element originally stored at a position `p`
with `i < p ≤ size - 1` has already been copied to position `p + 1`, and
positions `index` through `i` (inclusive) still hold their original,
unmodified values.*

- **Initialization:** Before the first iteration, `i = size - 1`. The set of
  positions `p` with `i < p ≤ size - 1` is empty, so the "already shifted"
  part of the invariant holds vacuously. Positions `index` through `size - 1`
  are still untouched, which is trivially true since no copy has happened
  yet. The invariant holds before the first iteration.

- **Maintenance:** Assume the invariant holds at the start of an iteration
  with a given `i` (so everything at positions `> i` has already been moved
  one slot to the right, and positions `index..i` are original). The loop
  body executes `data[i+1] = data[i]`, moving the original element at
  position `i` to position `i+1`. Because the loop processes indices from
  `size-1` down to `index`, position `i+1` was either (a) empty/irrelevant
  (if `i = size-1`, `i+1 = size`, a fresh slot after growth) or (b) already
  vacated by a previous iteration's copy *out* of it, so this write does not
  destroy any value that still needs to be read. After the assignment, the
  element originally at position `i` now correctly sits at `i+1`, extending
  the "already shifted" region down to include `i`, and decrementing `i`
  restores the invariant for the next iteration (now positions `> i-1`, i.e.
  `≥ i`, are shifted, and `index..i-1` remain original).

- **Termination:** The loop variable `i` strictly decreases each iteration
  and the loop stops once `i < index`. Since `i` starts at a finite value
  `size - 1` and decreases by exactly 1 each time, the loop is guaranteed to
  terminate after exactly `size - index` iterations.

- **Correctness at termination:** When the loop exits, `i = index - 1`, so by
  the invariant every original element at a position `p` with
  `index - 1 < p ≤ size - 1`, i.e. every element from `index` to `size - 1`,
  has been copied one slot to the right, and position `index` itself has
  not yet been overwritten. The statement `data[index] = x` that follows the
  loop can therefore safely place `x` at `index` without losing any data —
  the old element that was at `index` is now safely stored at `index + 1`.
  This establishes that after the full method runs, the array holds exactly
  the original elements in their original relative order, with `x` inserted
  at `index`, which is precisely the specification of `add(index, x)`.

### 3.2 `MinHeap.extractMin()` — sift-down loop

```java
private void siftDown(int i) {
    while (true) {
        int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
        if (left < size && at(left).compareTo(at(smallest)) < 0) smallest = left;
        if (right < size && at(right).compareTo(at(smallest)) < 0) smallest = right;
        if (smallest == i) break;
        swap(i, smallest);
        i = smallest;
    }
}
```

This loop runs after `extractMin()` moves the last leaf to the root and
shrinks the heap by one, so at the start of `siftDown(0)` every subtree
*except possibly the one rooted at index 0* already satisfies the heap
property (it was a valid heap before extraction, and removing/relocating
only the root and the former last leaf cannot break any other subtree).

**Loop invariant:** *At the start of each iteration (for the current index
`i`), every node in the heap array **except** those in the subtree rooted at
`i` satisfies the min-heap property (each node's key ≤ its children's keys),
and if the subtree rooted at `i` violates the property at all, the violation
is only possible at the root of that subtree (i.e. both of `i`'s children,
if they exist, already head valid min-heaps).*

- **Initialization:** Before the first iteration, `i = 0` and, as argued
  above, everything outside the subtree rooted at 0 (which is the whole
  heap minus the root) is untouched and was already valid; both children of
  the root (if the heap is large enough to have them) are themselves roots
  of subtrees that were valid before the extraction and are unaffected by
  it, so the invariant holds at the start.

- **Maintenance:** Assume the invariant holds for the current `i`. The loop
  computes `smallest` as the index of the minimum among `data[i]` and its
  existing children — this uses exactly the comparisons needed and no more,
  since a node with no left child also has no right child in a complete
  binary tree, so the `left < size` / `right < size` guards correctly skip
  non-existent children. Two cases:
    1. If `smallest == i`, the current root of the subtree is already ≤ both
       its children, so the whole subtree satisfies the heap property and the
       loop breaks — consistent with the invariant now holding everywhere.
    2. Otherwise, swapping `data[i]` and `data[smallest]` places the smaller
       of the two at position `i`. Because both children's subtrees were
       already valid heaps *before* the swap (by the invariant) and swapping
       only replaces the value at their shared parent and at one child's root
       with values ≥ their own children (since a min-heap property held
       within each child's subtree beforehand, and the swap only inserts, at
       the child position, a value that is ≤ its former sibling and ≤ the
       original root, hence still ≥ its own subtree's minimum was preserved as
       a candidate — this is precisely why we pick the *smaller* of the two
       children), the subtree rooted at `i` now has a correct root relative to
       its immediate children, and the only place a violation can remain is
       within the subtree now rooted at `smallest` (the node that received the
       old, possibly-too-large root value). Setting `i = smallest` and
       continuing the loop restores exactly the stated invariant for the new
       `i`.

- **Termination:** Each iteration either breaks immediately or moves `i` one
  level deeper into the tree (`i` becomes a child index, which is always
  strictly greater than the parent index in this array encoding, and is
  bounded above by `size`). Since the tree has a finite height
  ⌊log₂ size⌋ + 1, and `i` strictly increases in value while remaining a
  valid index, the loop can execute at most ⌊log₂ size⌋ times before either
  breaking or reaching a leaf (a node with no children), at which point both
  guards `left < size` and `right < size` fail, `smallest` stays `i`, and
  the loop breaks. Termination is therefore guaranteed.

- **Correctness at termination:** When the loop exits, `smallest == i`,
  which by the invariant's own case analysis means the subtree rooted at the
  current `i` now satisfies the heap property, and by the invariant
  everything outside that subtree already did. Hence the *entire* array
  satisfies the min-heap property, which is exactly the postcondition
  `extractMin()` must establish before returning. Combined with the fact
  that the returned value was the minimum of the heap *before* the loop
  started (captured before any modification), this proves `extractMin()`
  correctly removes and returns the minimum while leaving a valid heap
  behind.

## 4. Experimental Setup

- **n** (initial elements): 100; 1,000; 10,000; 100,000, as required.
- **m** (operations per workload): 10,000 `get()` calls (Workload 1); 1,000
  `contains()` calls (Workload 2); 1,000 insertions and 1,000 removals, at
  both the beginning and the middle (Workload 3); n `insert()` and n
  `extractMin()` calls (Workload 4).
- **Repetitions:** every timed measurement is run 5 times and the CSVs
  report the **average** of those 5 runs.
- **Timing method:** `System.nanoTime()`, wrapped tightly around only the
  operation loop being measured; input generation (`generateData`, building
  the initial structure, generating the indices/values to use) always
  happens **before** the timer starts.
- **Random seed:** `new Random(42 + rep)` — a fixed, reproducible base seed
  of 42 as suggested by the assignment, offset by the repetition number so
  the 5 repeats use different-but-reproducible data rather than measuring
  the exact same operation sequence five times.
- **Special case handled honestly:** Workload 3 asks for 1,000 removals at a
  fixed index, but for small n (e.g. n=100, removing at index 0) the
  structure runs out of elements after only n removals. Rather than invent
  data or crash, the benchmark stops removing once the target index is no
  longer valid, and records the *actual* number of removals performed in the
  `ops_performed` column of `workload3_insertion_removal.csv`. For n ≥ 1000
  at the beginning position, all 1,000 removals complete normally; for the
  middle position, at most `n/2` removals are possible before the middle
  index runs off the shrinking structure, which is also recorded.

Source: `src/Benchmark.java`. Raw results: `results/tables/*.csv`. Plots:
`results/plots/*.png`, generated by `scripts/plot_results.py`.

## 5. Results

### 5.1 Workload 1 — Random Access (10,000 `get()` calls)

| n | DynamicArray avg time | LinkedList avg time | Theoretical |
|---|---|---|---|
| 100 | 0.18 ms | 3.05 ms | O(1) vs O(n) |
| 1,000 | 0.04 ms | 16.28 ms | O(1) vs O(n) |
| 10,000 | 0.01 ms | 91.58 ms | O(1) vs O(n) |
| 100,000 | 0.05 ms | 1,143.79 ms | O(1) vs O(n) |

![Workload 1 time](results/plots/workload1_time_vs_n.png)
![Workload 1 accesses](results/plots/workload1_accesses_vs_n.png)

### 5.2 Workload 2 — Search (1,000 `contains()` calls)

| n | DynamicArray avg time | LinkedList avg time | Comparisons (both) | Theoretical |
|---|---|---|---|---|
| 100 | 2.29 ms | 1.44 ms | 100,000 | O(n) |
| 1,000 | 0.63 ms | 2.36 ms | 1,000,000 | O(n) |
| 10,000 | 6.92 ms | 24.70 ms | 9,946,179 | O(n) |
| 100,000 | 70.54 ms | 280.54 ms | 95,408,037 | O(n) |

![Workload 2 time](results/plots/workload2_time_vs_n.png)
![Workload 2 comparisons](results/plots/workload2_comparisons_vs_n.png)

### 5.3 Workload 3 — Insertion and Removal (1,000 ops, beginning and middle)

Full table: `results/tables/workload3_insertion_removal.csv`. Highlights:

| n | Structure | Position | Op | Avg time |
|---|---|---|---|---|
| 100,000 | DynamicArray | beginning | insert | 86.32 ms |
| 100,000 | LinkedList | beginning | insert | 1.53 ms |
| 100,000 | DynamicArray | beginning | remove | 79.23 ms |
| 100,000 | LinkedList | beginning | remove | 0.01 ms |
| 100,000 | DynamicArray | middle | insert | 35.70 ms |
| 100,000 | LinkedList | middle | insert | 125.58 ms |

![WL3 time beginning](results/plots/workload3_time_vs_n_beginning.png)
![WL3 time middle](results/plots/workload3_time_vs_n_middle.png)
![WL3 movements beginning](results/plots/workload3_movements_vs_n_beginning.png)
![WL3 movements middle](results/plots/workload3_movements_vs_n_middle.png)

### 5.4 Workload 4 — Priority Processing (Min-Heap)

| n | Avg insert time (n calls) | Avg extractMin time (n calls) | Insert comparisons | Extract comparisons | Non-decreasing verified |
|---|---|---|---|---|---|
| 100 | 0.068 ms | 0.473 ms | 206 | 862 | true |
| 1,000 | 0.197 ms | 1.874 ms | 2,265 | 14,972 | true |
| 10,000 | 1.569 ms | 4.233 ms | 23,083 | 216,603 | true |
| 100,000 | 6.063 ms | 29.240 ms | 228,402 | 2,831,248 | true |

![WL4 time](results/plots/workload4_time_vs_n.png)
![WL4 comparisons](results/plots/workload4_comparisons_vs_n.png)

## 6. Discussion — Theory vs. Experiment

**Workload 1 (Random Access).** The results agree strongly with theory:
`DynamicArray.get()` stays roughly flat (tens of microseconds) regardless of
n, exactly as expected for Θ(1) direct indexing, while `LinkedList.get()`
grows essentially linearly with n — at n=100,000 it is over **20,000× slower**
than the array for the same 10,000 lookups, because each lookup must walk, on
average, n/2 pointers. The `accesses` column confirms this directly: the
array always performs exactly 10,000 accesses (one per call) no matter how
large n is, while the list's total pointer traversals scale with n (from
~512,000 at n=100 to over 500 million at n=100,000).

**Workload 2 (Search).** Both structures are Θ(n) here, and the comparison
counts are *identical* between the two (100,000 → 95.4 million as n grows),
confirming that `contains()` inherently does the same amount of logical work
regardless of the underlying structure. The measured *times* still differ:
the array is consistently faster than the list (up to ~4× at n=100,000)
even though both perform the same number of comparisons. This is a direct,
measured illustration of point 4/5 in Section 7: two algorithms with the
same Big-O complexity can have very different constant factors — sequential
array access has excellent CPU cache locality (elements are contiguous in
memory), while chasing pointers between heap-allocated linked-list nodes
causes many more cache misses per comparison.

**Workload 3 (Insertion/Removal).** This workload shows the clearest
structural trade-off. At the **beginning** (index 0): the array must shift
essentially the whole structure for every one of the 1,000 insertions/
removals, so its cost grows with n (86.3 ms at n=100,000), while the list's
`add(0,x)`/`remove(0)` are O(1) and stay under 2 ms even at n=100,000 — a
difference of roughly 50–8,000×, matching the O(n) vs O(1) prediction. At
the **middle** (index n/2), the picture flips for insertion: the array is
*faster* than the list at large n (35.7 ms vs 125.6 ms at n=100,000),
because although both are O(n) here, the array's cost is a fast contiguous
`System.arraycopy`-style shift, while the list must still *traverse* n/2
pointers just to *find* the middle before it can insert — the list pays the
full O(n) traversal cost plus the insertion, whereas the array's O(n) is a
tight memory-move loop with much better cache behavior. This is a second
concrete example of same-Big-O-different-constant-factor behavior.

**Workload 4 (Priority Processing).** `extractMin()` is consistently more
expensive than `insert()` at every n (e.g. at n=100,000: 29.2 ms total for
extraction vs 6.1 ms for insertion), even though both are Θ(log n) per call.
This matches theory once the *comparison counts* are examined: extraction
needs up to 2 comparisons per level (left child and right child) while
insertion needs only 1 comparison per level (against the parent) — so
extraction does roughly twice the per-level work, and on top of that,
elements extracted early from a nearly-full heap sift down through more
full levels on average than elements inserted into a heap that is still
growing. `non_decreasing_verified` is `true` for every n, confirming the
heap always produces a fully sorted extraction order (heapsort-style),
which is only possible if the min-heap property was correctly maintained
after every single operation.

**Where results deviate from theory (and why).** At the smallest input size
(n=100), several measurements are noisier or even non-monotonic — e.g.
`DynamicArray` random-access time is *higher* at n=100 than n=1,000 in
Workload 1 (0.18 ms vs 0.04 ms), and Workload 3's `DynamicArray insert` at
the beginning is higher at n=100 than n=1,000 (3.10 ms vs 1.25 ms). This is
expected and is a JIT warm-up / measurement-noise effect, not a violation of
the complexity analysis: at n=100, the very first benchmark method invoked
for a structure is also the very first time the JVM's just-in-time compiler
sees that method's bytecode, so it runs in slower interpreted mode before
being compiled to native code; by n=1,000 the JIT has already optimized the
hot loop from the n=100 run. For n ≥ 1,000 the results track the predicted
asymptotic trends cleanly.

## 7. Performance and Design Analysis

1. **How does increasing n affect each workload?** For O(1)-per-call
   operations (array `get`, list `add`/`remove` at index 0), time stays flat
   as n grows. For O(n)-per-call operations (array `add`/`remove` at 0,
   list `get`/`contains`/middle operations), time grows roughly linearly
   with n. For the heap's O(log n) operations, time grows very slowly —
   n grew 1,000× (100 → 100,000) while heap insert time grew only ~89×.
2. **Which results agree with theory?** All of them, in the sense that
   every measured trend (flat, linear, logarithmic) matches its predicted
   Big-O class once n is large enough to dominate constant-factor and JIT
   noise (n ≥ 1,000 in this experiment).
3. **Where do results differ from prediction?** Only at n=100, due to JIT
   warm-up noise (Section 6) — not a genuine complexity mismatch — and in
   the relative *ranking* between structures with equal Big-O (Workload 2,
   and Workload 3's middle case), where identical complexity classes still
   produced different absolute times because of cache locality.
4. **Why can two algorithms with the same Big-O have different running
   times?** Big-O only counts how the number of "basic operations" scales
   with n — it hides the constant factor per operation. Contiguous memory
   access (arrays) is dramatically faster per operation than pointer-chasing
   (linked lists) on real hardware due to CPU cache behavior, even when both
   perform the exact same number of logical comparisons (Workload 2).
5. **How do constant factors and implementation details affect
   performance?** Directly and substantially, as shown above: the array
   beat the list by up to 4× in Workload 2 and by ~3.5× in Workload 3's
   middle-insertion case, purely from memory layout, despite equal
   asymptotic complexity.
6. **Why is a Dynamic Array preferable for some workloads?** Whenever
   random access by index matters (Workload 1) or when most insert/remove
   traffic happens at the end, the array's O(1) `get` and cache-friendly
   contiguous layout make it the clear winner, and it also wins at
   middle-position insertion/removal for large n because traversal cost is
   avoided.
7. **When can a Linked List be useful?** When insertions/removals are
   concentrated at the front (or, more generally, at a position already
   reached by an existing reference/iterator) and random access is not
   needed — Workload 3's beginning-position results show the list winning
   by 50–8,000×.
8. **Why is a Heap appropriate for priority-based processing?** It gives
   O(log n) insert and extract-min, and O(1) peek, without needing the
   collection to be fully sorted at any point — Workload 4 confirms both
   operations scale logarithmically and that extraction order is always
   correctly non-decreasing.
9. **How does the workload influence the choice of data structure?** The
   same two structures switch from an 8,000× to a ~0.3× relative speed
   advantage between Workload 3's beginning and middle positions purely
   because of *where* the operations occur — the workload's access pattern,
   not the data itself, determines which structure is faster.

## 8. Conclusion

The experiments confirm the theoretical complexity analysis for every
required operation: Dynamic Array `get` is O(1) and vastly outperforms
Linked List's O(n) `get`; both structures' `contains` is O(n) with nearly
identical operation counts but different constants due to memory locality;
insertion/removal cost is dominated by *where* the operation happens
(beginning favors the list, middle can favor the array once traversal cost
is accounted for); and the Min-Heap delivers the expected O(log n)
insert/extractMin with a fully verified non-decreasing extraction order at
every tested size. The main lesson reinforced by the numbers is that
Big-O complexity predicts *how performance scales* correctly in every case
tested, but does not predict *absolute* performance — cache locality and
constant factors, clearly visible in Workloads 2 and 3, can and do change
which structure is faster even when their asymptotic complexity is
identical.

## 9. Project Structure

```
assignment-2/
├── src/
│   ├── DynamicArray.java
│   ├── LinkedList.java
│   ├── MinHeap.java
│   ├── Benchmark.java
│   └── Tests.java
├── scripts/
│   └── plot_results.py
├── results/
│   ├── tables/         (4 CSV files, one per workload)
│   └── plots/          (10 PNG files, 2+ per workload)
└── README.md
```

## 10. How to Run

```bash
# Compile
cd src
javac -d ../out DynamicArray.java LinkedList.java MinHeap.java Tests.java Benchmark.java

# Run the test suite (7,183 assertions)
cd ../out
java Tests

# Run all four benchmarks (writes CSVs to results/tables)
java Benchmark ../results/tables

# Regenerate plots from the CSVs
cd ../scripts
python3 plot_results.py
```
