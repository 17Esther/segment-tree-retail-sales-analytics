# GenAI Usage Log

This file is a running record of how Generative AI tools were used during
the assignment. **Fill this in as you work** — do not reconstruct it at the
end. Authentic entries are noticeably more specific (actual prompts, actual
AI outputs you kept or rejected) than retrospective ones.

The assignment requires at least **three** distinct GenAI tools. A plausible
split is below — swap in whatever you actually use.

| Tool            | Typical role                                     |
|-----------------|--------------------------------------------------|
| ChatGPT / Claude| Design discussion, algorithm explanation         |
| GitHub Copilot  | In-editor autocomplete of boilerplate / tests    |
| Google Gemini   | A second-opinion code review; cross-check bugs   |

------------------------------------------------------------------

## Entry Template (copy per interaction)

**Date/Time:**
**Tool:**
**Stage of work:** (design / coding / debugging / optimisation / documentation)
**Prompt I gave:**
> ...

**Key AI output (summary, not full paste):**
> ...

**What I accepted:**
- ...

**What I rejected and why:**
- ...

**Change I made to the AI suggestion:**
- ...

------------------------------------------------------------------

## Example Seed Entries (replace with your real interactions)

### Entry 1 — Design discussion
**Date/Time:** 2026-04-15 20:10
**Tool:** ChatGPT (GPT-4o) / Claude / ...
**Stage of work:** design
**Prompt I gave:**
> I want to implement a Segment Tree in Java for a daily retail sales scenario.
> I need point updates and range sums, and I want to justify whether adding
> lazy propagation is worth it. What trade-offs should I weigh?

**Key AI output (summary):**
> Explained that lazy propagation pays off when range updates are frequent.
> Suggested the array-backed implementation with 4n space and recommended
> using a parallel lazy[] array. Warned about the "tree[node] current vs.
> lazy[node] pending for children" convention.

**What I accepted:**
- Array-backed layout with 4n sizing.
- The "tree[node] always current, lazy[node] pending for children" convention.

**What I rejected and why:**
- Rejected a suggestion to use a generic `SegmentTree<T>` with a
  `BinaryOperator<T>` parameter. My scenario is concretely about sums of
  euros, and a generic version would add a lot of boilerplate without
  benefit — the assignment rewards clarity, not feature bloat.

**Change I made to the AI suggestion:**
- The AI initially proposed storing longs without documenting that cents are
  used. I explicitly documented that monetary values are in cents to avoid
  double-precision drift.

------------------------------------------------------------------

### Entry 2 — Code review
**Date/Time:**
**Tool:** (e.g. Google Gemini / Claude — must differ from Entry 1)
**Stage of work:** debugging / code review
**Prompt I gave:**
> Here is my SegmentTree.java. Review for correctness, especially around
> lazy propagation. Identify any bugs or style issues.

**Key AI output:**
> ...

**What I accepted / rejected / changed:**
> ...

------------------------------------------------------------------

### Entry 3 — IDE autocomplete
**Date/Time:**
**Tool:** GitHub Copilot / Tabnine / ...
**Stage of work:** coding
**Notes:**
> Copilot suggested the skeleton of `assertThrows`. I reworked the parameter
> order to match JUnit style. It also suggested `r.run()` inside a plain
> try/catch which I kept.

------------------------------------------------------------------

## Summary of AI Disagreement / Where Human Judgement Mattered

Write **at least three concrete bullets** here when you finish. Examples:

- When ChatGPT produced a version of `push()` that applied the lazy value to
  `tree[node]` *again* before pushing to children, I caught the double-count
  against my randomised differential tests and rewrote the method.
- When Copilot completed `rangeSum` with `(start + end) / 2`, I changed it
  to `(start + end) >>> 1` after reading about the binary-search overflow
  bug. This is the kind of call the AI would not make by default.
- When Gemini suggested switching the whole project to a segment tree that
  stores *minimum* values instead of sums, I rejected it — the domain model
  is additive, and it misread my prompt.
