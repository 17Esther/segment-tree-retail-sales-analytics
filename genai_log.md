# GenAI Usage Log

**Module:** COMP47500 — Advanced Data Structures in Java (2025/26 Spring)  
**Author:** Jiawei Li (25212461)  
**Data Structure:** Segment Tree with lazy propagation  
**Scenario:** Daily retail sales analytics

This log documents how three Generative AI tools were used during
the assignment. Each entry records the prompt (paraphrased where exact
wording was not saved), the key AI output, and what I accepted, rejected,
or changed.

---

## Tool summary

| Tool              | Role                                                      |
|-------------------|-----------------------------------------------------------|
| ChatGPT (GPT-5.4) | Understanding the brief, planning, scenario ideas, optimisations |
| Claude (Opus 4.6) | Draft code generation, algorithm explanation, debugging    |
| Google Gemini      | Second-opinion code review, optimisation suggestions       |

---

## Entry 1 — Understanding the brief and planning

**Tool:** ChatGPT (GPT-5.4)  
**Stage of work:** design / planning

**Prompt I gave:**  
> Here is my COMP47500 assignment brief. Can you summarise what I need to
> deliver, and turn it into a short checklist I can work through?  
> [Pasted assignment brief]


**Key AI output (summary):**  
> Produced a numbered checklist: (1) choose a data structure, (2) implement
> core operations from scratch, (3) build a test harness, (4) write a
> performance discussion, (5) use at least three GenAI tools and log usage,
> (6) write a ~1,000-word reflective report with sections A/B/C, (7) include
> an AI usage declaration.

**What I accepted:**  
- The checklist structure — it helped me break the assignment into concrete
  steps instead of treating it as one large task.

**What I rejected and why:**  
- Nothing significant; this was a straightforward summarisation task.

**Change I made:**  
- Reordered the checklist to front-load the data structure choice and
  scenario design, since everything else depends on that decision.

---

## Entry 2 — Scenario brainstorming

**Tool:** ChatGPT (GPT-5.4)  
**Stage of work:** design

**Prompt I gave:**  
> I'm choosing a Segment Tree for this assignment. Suggest some real-world
> scenarios where a Segment Tree with lazy propagation would be a natural
> fit. I need range-sum queries, point updates, and range updates.


**Key AI output (summary):**  
> Proposed several scenarios: (1) retail sales analytics — daily revenue
> with corrections and bulk adjustments, (2) IoT sensor monitoring —
> aggregate readings over time windows, (3) game leaderboards — score
> ranges and bulk score adjustments.

**What I accepted:**  
- Chose the retail sales analytics scenario. The operation mix (range-sum
  for revenue queries, point-update for POS corrections, range-add for
  supplier rebates) maps directly onto the Segment Tree API.

**What I rejected and why:**  
- The sensor monitoring and leaderboard ideas were plausible but less
  natural for demonstrating all three operation types in a single
  convincing narrative.

---

## Entry 3 — Draft code generation

**Tool:** Claude (Opus 4.6)  
**Stage of work:** coding

**Prompt I gave:**  
> Implement a Segment Tree in Java with lazy propagation supporting build,
> pointUpdate, rangeAdd, and rangeSum. Use a 1-indexed array layout with
> 4n sizing. Also create a SalesAnalytics wrapper class that translates
> calendar dates into tree indices. Store monetary values as long cents.


**Key AI output (summary):**  
> Produced draft implementations of `SegmentTree.java` and
> `SalesAnalytics.java`, plus a `Main.java` demo, a `SegmentTreeTest.java`
> test harness with both hand-written edge cases and a randomised
> differential test, and a `PerformanceBenchmark.java` comparing the
> segment tree against a naive O(n) array baseline. Used the `tree[node]`
> always-current / `lazy[node]` pending-for-children convention.

**What I accepted:**  
- The overall class structure and separation of concerns between
  `SegmentTree` and `SalesAnalytics`.
- The randomised differential testing strategy (cross-checking against a
  naive baseline).
- The benchmark structure with JIT warm-up and a checksum to prevent
  dead-code elimination.

**What I rejected and why:**  
- The initial code used a package declaration
  (`package ie.ucd.comp47500.segmenttree;`) which complicated the compile
  and run commands. I removed it to keep the submission simple for the
  grader.

**Changes I made:**  
- Reviewed and modified all generated code; added my own comments
  explaining design intent (especially around lazy propagation).
- Changed the benchmark to sweep multiple input sizes automatically
  rather than running a single size.
- Rewrote the test output to be quieter during the randomised section
  (silent on pass, verbose on failure, single summary line).

---

## Entry 4 — Algorithm explanation and debugging

**Tool:** Claude (Opus 4.6)  
**Stage of work:** debugging

**Prompt I gave:**  
> Walk me through what happens step by step when I call rangeAdd(2, 5, 3)
> on an 8-element tree, then call rangeSum(3, 6). Show me how the lazy
> values propagate through push().


**Key AI output (summary):**  
> Walked through the tree node by node: which nodes are fully contained
> and receive the lazy delta directly, which require a partial-overlap
> push, and how tree[node] is updated immediately while lazy[node] defers
> work to children. Then traced the rangeSum query showing where push()
> fires and how previously deferred deltas reach the leaves.

**What I accepted:**  
- The step-by-step trace clarified the invariant: `tree[node]` is always
  the correct aggregate for its segment; `lazy[node]` only records what
  has not yet been pushed to children.

**What I rejected and why:**  
- An earlier draft of the `push()` method (from a previous coding
  interaction) had applied the lazy delta to `tree[node]` a second time
  before pushing to children — a double-counting bug. I caught this
  through my randomised differential test (5,000 random operations
  disagreed with the naive baseline). I brought the failing test output
  back to Claude, which helped me trace the root cause: the delta was
  already reflected in `tree[node]` at accumulation time, so applying it
  again in `push()` was incorrect.

**Changes I made:**  
- Rewrote `push()` to only propagate the delta downward to children
  without touching `tree[node]` again. Re-ran the differential test to
  confirm all 5,000+ assertions passed.

---

## Entry 5 — Optimisation suggestions

**Tool:** ChatGPT (GPT-5.4)  
**Stage of work:** optimisation

**Prompt I gave:**  
> Review my SegmentTree.java for any common pitfalls or small
> optimisations I might be missing.


**Key AI output (summary):**  
> Suggested two things: (1) use `(start + end) >>> 1` instead of
> `(start + end) / 2` to avoid the classic signed-integer overflow bug in
> midpoint computation, and (2) confirmed that storing monetary values as
> `long` cents rather than `double` euros avoids floating-point drift and
> keeps the differential test deterministic.

**What I accepted:**  
- The unsigned-shift midpoint — a genuine correctness improvement.
- The `long` cents confirmation reinforced a decision I was already
  leaning towards.

**What I rejected and why:**  
- Nothing rejected in this interaction.

---

## Entry 6 — Second-opinion code review

**Tool:** Google Gemini  
**Stage of work:** code review / optimisation

**Prompt I gave:**  
> Here is my final SegmentTree.java. Review the lazy propagation logic
> for correctness and suggest any improvements.  
> [Pasted SegmentTree.java]


**Key AI output (summary):**  
> Confirmed the lazy propagation implementation was correct. Then made two
> additional suggestions: (1) switch the tree to store minimum values
> instead of sums, and (2) use JMH (Java Microbenchmark Harness) for
> performance measurement.

**What I accepted:**  
- The confirmation that the lazy logic was sound (useful as a cross-check
  after fixing the earlier `push()` bug).

**What I rejected and why:**  
- Rejected the minimum-tree suggestion — the retail domain model is
  additive (total revenue over a range), not about finding minimums.
  Gemini appeared to misread my prompt.
- Rejected the JMH suggestion — for a 10% assignment, pulling in a
  third-party benchmarking framework would complicate the build and
  submission. I kept a hand-rolled benchmark with a JIT warm-up pass and
  a blackhole pattern to prevent dead-code elimination, which is
  sufficient for demonstrating the asymptotic gap.

---

## Summary of AI Disagreement / Where Human Judgement Mattered

- **The `push()` double-counting bug.** An earlier Claude-generated draft
  of `push()` applied the lazy delta to `tree[node]` again before pushing
  to children. The code looked clean and plausible, but my randomised
  differential test caught the disagreement immediately. This is the
  strongest example of why automated tests are essential even (especially)
  when AI generates the code.

- **Gemini's minimum-tree suggestion.** When asked to review my
  sum-based Segment Tree, Gemini suggested switching to store minimum
  values. The domain model is additive — total revenue, not lowest sale —
  so this was simply wrong for my scenario. I rejected it without
  hesitation, but it illustrates the "confidently irrelevant" failure
  mode where an AI tool answers a different question than the one asked.

- **Over-engineering pressure.** Across all three tools, I received
  suggestions to add complexity: a generic `SegmentTree<T>` with
  `BinaryOperator<T>`, JMH benchmarking, Maven build scripts. Each of
  these is reasonable in a production setting but would have increased
  submission friction for a focused assignment. Knowing when to say "no"
  to a plausible suggestion required understanding the context and scope
  of the task — something the AI tools could not judge for me.

- **The unsigned-shift midpoint.** ChatGPT recommended `(start + end) >>> 1`
  over `(start + end) / 2`. This is a genuine correctness improvement
  (prevents signed-integer overflow for very large indices), and I
  accepted it — but verifying why it matters required me to look up
  Bentley's original binary search bug rather than trusting the
  suggestion blindly.

- **Scenario and architecture choices.** The decision to use retail sales
  analytics, to separate `SegmentTree` from `SalesAnalytics`, to store
  values as `long` cents, and to design the test strategy (randomised
  differential + hand-written edge cases) were all human decisions. AI
  tools provided ingredients, but the overall shape of the project
  reflects my own judgement about what would make a clear and convincing
  submission.
