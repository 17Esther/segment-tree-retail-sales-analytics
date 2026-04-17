# GenAI-Enhanced Advanced Data Structures

**Module:** COMP47500 — Advanced Data Structures in Java (2025/26 Spring)
**Data Structure:** Segment Tree (with lazy propagation)
**Scenario:** Daily retail sales analytics
**Author:** Jiawei Li (25212461)
**Word target:** ~1,000–1,200 words (excluding AI declaration)

> **How to use this scaffold.** Each section has a target length and a short
> set of prompts inside *italics*. Do not leave the italic prompts in the
> final submission — answer them in your own words, then delete.
> Treat all text below as a starting skeleton you must rewrite in your own
> voice. The grader is explicitly looking for evidence that you did not
> submit AI prose.

---

## A. Data Structure Design (≈ 300 words)

*Answer in this section:*

- *Why did you choose a Segment Tree over the other options (AVL, Red-Black,
  Hash, Fibonacci Heap, etc.)? Tie this back to the retail analytics scenario:
  range-sum queries + point updates + bulk adjustments map naturally to a
  segment tree.*
- *What key design decisions did you make?* Examples you can cite:
  - Array-backed layout with 1-indexed nodes; size 4n to sidestep non-power-of-two edge cases.
  - Lazy propagation with the convention `tree[node]` always current,
    `lazy[node]` pending for children (contrast with the alternative
    convention and explain why yours is cleaner for queries).
  - Using `long` cents instead of `double` euros to avoid floating-point drift.
  - Separating `SegmentTree` (data structure) from `SalesAnalytics` (domain
    wrapper) — so the data structure is reusable.
  - `(start + end) >>> 1` vs `(start + end) / 2` to avoid the Bentley-style
    overflow bug.
- *What trade-offs did you make?*
  - Rejected a generic `SegmentTree<T, BinaryOperator<T>>`: more flexible
    but adds boilerplate and obscures the core algorithm. Assignment rewards
    clarity.
  - Rejected a Fenwick / BIT: simpler code but does not support range
    updates in O(log n) without two BITs, and the implementation nuance is
    less pedagogically rich.

---

## B. Use of Generative AI (≈ 400 words)

*Answer in this section:*

- *Which GenAI tools did you use, in what order, and why those three?*
  Mention at least three distinct tools. Typical split: one conversational
  model for design discussion (ChatGPT / Claude), one IDE-embedded
  tool for completions (GitHub Copilot / Tabnine), one second-opinion
  tool for code review (Gemini / a second LLM / SpellBox).
- *How did each tool support your work across the four permitted use cases
  (design exploration, code generation, debugging, optimisation)?* Link
  each claim to a concrete entry in `genai_log.md`.
- *What worked well?* Examples:
  - Rapidly bouncing design trade-offs (lazy-propagation convention,
    generic vs. monomorphic) off a conversational model.
  - Copilot autocomplete was very effective for boilerplate like the
    `assertEquals` / `assertThrows` helpers.
- *What did not work well?* Examples (make these real — the marker will
  spot generic platitudes):
  - A tool initially produced a `push()` method that applied the lazy
    value to `tree[node]` *again* — a subtle double-counting bug that my
    randomised differential test caught immediately. This is a strong
    argument for why tests remain essential even with AI assistance.
  - When asked for "the best way to benchmark", one tool suggested pulling
    in JMH. For a 10% undergraduate assignment, that is overkill and would
    have complicated the submission. I kept a hand-rolled benchmark with a
    warm-up and a JIT-defeating "blackhole" pattern instead.
  - A tool suggested storing monetary values as `double`. I overruled this
    and used `long` cents; floating-point drift would have made the
    differential test flaky.
- *Where was human judgement essential?* Examples: the choice of scenario,
  the boundary between data structure and domain wrapper, deciding what
  *not* to add, and designing the test strategy itself.

---

## C. Critical Reflection (≈ 300 words)

*Answer in this section:*

- *Did GenAI improve your understanding?* Be honest. If the answer is
  "yes, for the high-level intuition around lazy propagation, but I still
  had to re-derive the `push()` logic by hand to trust it", say so.
- *Did it introduce risks or misconceptions?* Concrete examples:
  - The "confidently wrong" failure mode — a plausible-looking `push()`
    that was subtly incorrect. My differential test caught it; a hand-only
    code review probably would not have.
  - Tendency to over-engineer (generic types, JMH, feature creep).
  - Potential attribution drift — it is easy to accept an AI suggestion
    verbatim and forget it was AI-authored. The running `genai_log.md`
    helped prevent this.
- *How would you use (or limit) GenAI in future software engineering?*
  Draw a principled line. Examples:
  - Use for: scaffolding, boilerplate, explanation of unfamiliar idioms,
    second-opinion code review, refactoring suggestions.
  - Do not use without strong tests when the code involves subtle
    invariants (lazy propagation, lock-free concurrency, numerical stability).
  - Always keep the tests that verify AI-authored code, not just the code
    itself.

---

## AI Usage Declaration

I used the following Generative AI tools while completing this assignment:

- **[Tool 1]** — used for [design / code / debugging / ...]. Representative
  prompts and decisions are documented in `report/genai_log.md`.
- **[Tool 2]** — used for [...].
- **[Tool 3]** — used for [...].

All final design decisions, algorithmic reasoning, test strategy, and report
prose are my own. Where I have reused or adapted AI-produced code, I have
reviewed and modified it and kept a record of the changes in
`report/genai_log.md`. I accept full responsibility for the correctness of
the submitted code.

Signed: _________________________   Date: ______________
