/**
 * A Segment Tree with lazy propagation, supporting:
 *
 *   - build(long[])          : O(n)        construct from an initial array
 *   - pointUpdate(i, v)      : O(log n)    overwrite element at index i
 *   - rangeAdd(l, r, delta)  : O(log n)    add delta to every element in [l, r]
 *   - rangeSum(l, r)         : O(log n)    sum of every element in [l, r]
 *
 * Why lazy propagation?
 *   A plain Segment Tree answers range-sum queries in O(log n) but still
 *   needs O((r - l) log n) for a naive range-add implemented as many point
 *   updates. Lazy propagation fixes this by letting a fully-contained
 *   ancestor node record a pending "add delta to every child" update
 *   without recursing further. The update is only pushed down when a later
 *   query/update actually needs to descend through that node. Both
 *   rangeAdd and rangeSum then share the same O(log n) bound, which is the
 *   whole point of using a Segment Tree for the retail analytics scenario.
 *
 * Layout:
 *   The tree is stored in a 1-indexed array (index 0 unused). Node k has
 *   children at 2k and 2k+1. A safe upper bound on the array length is
 *   4 * n for any n >= 1, so we allocate exactly that.
 *
 * Invariants:
 *   tree[node]  is ALWAYS the current aggregate for the segment it covers.
 *   lazy[node]  is a pending delta that has NOT yet been applied to node's
 *               children; it has already been applied to tree[node].
 *
 * This convention keeps queries simple: when a node is fully contained in
 * the query range, its tree[node] is returned directly without touching
 * lazy[].
 *
 * Author: Jiawei Li (25212461), COMP47500 2025/26
 */
public class SegmentTree {

    /** Underlying data length. */
    private final int n;

    /** Aggregate (sum) per segment, 1-indexed. Size 4n for safety. */
    private final long[] tree;

    /** Pending add-delta for children of each node. Same shape as tree. */
    private final long[] lazy;

    // --------------------------------------------------------------
    // Construction
    // --------------------------------------------------------------

    /**
     * Build a Segment Tree over a copy of {@code data}. O(n) time.
     *
     * @throws NullPointerException     if data is null
     * @throws IllegalArgumentException if data is empty
     */
    public SegmentTree(long[] data) {
        if (data == null) {
            throw new NullPointerException("data must not be null");
        }
        if (data.length == 0) {
            // Empty input is rejected explicitly. Every public method
            // (rangeSum, rangeAdd, pointUpdate, get) operates on indices,
            // and none of them is meaningful on an empty array. Failing
            // fast at construction is clearer than returning a tree that
            // throws on every subsequent call.
            throw new IllegalArgumentException("data must not be empty");
        }
        this.n = data.length;
        // 4n covers the pathological case where n is not a power of two.
        int size = 4 * n;
        this.tree = new long[size];
        this.lazy = new long[size];
        build(1, 0, n - 1, data);
    }

    /** Recursive bottom-up build. */
    private void build(int node, int start, int end, long[] data) {
        if (start == end) {
            tree[node] = data[start];
            return;
        }
        int mid = (start + end) >>> 1;        // unsigned shift = safe midpoint
        build(2 * node,     start,   mid, data);
        build(2 * node + 1, mid + 1, end, data);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    // --------------------------------------------------------------
    // Public API
    // --------------------------------------------------------------

    /** @return size of the underlying array. */
    public int size() {
        return n;
    }

    /** Overwrite element at index {@code idx} with {@code value}. */
    public void pointUpdate(int idx, long value) {
        validateIndex(idx);
        pointUpdate(1, 0, n - 1, idx, value);
    }

    /** Add {@code delta} to every element in the inclusive range [l, r]. */
    public void rangeAdd(int l, int r, long delta) {
        validateRange(l, r);
        rangeAdd(1, 0, n - 1, l, r, delta);
    }

    /** @return sum of elements in the inclusive range [l, r]. */
    public long rangeSum(int l, int r) {
        validateRange(l, r);
        return rangeSum(1, 0, n - 1, l, r);
    }

    /** Convenience wrapper: read a single element. O(log n). */
    public long get(int idx) {
        return rangeSum(idx, idx);
    }

    // --------------------------------------------------------------
    // Internal recursion
    // --------------------------------------------------------------

    private void pointUpdate(int node, int start, int end, int idx, long value) {
        if (start == end) {
            // Leaf: no children to propagate to, so clearing lazy here
            // is just a tidiness choice.
            tree[node] = value;
            lazy[node] = 0L;
            return;
        }
        push(node, start, end);
        int mid = (start + end) >>> 1;
        if (idx <= mid) {
            pointUpdate(2 * node, start, mid, idx, value);
        } else {
            pointUpdate(2 * node + 1, mid + 1, end, idx, value);
        }
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    private void rangeAdd(int node, int start, int end, int l, int r, long delta) {
        if (r < start || end < l) {                       // disjoint
            return;
        }
        if (l <= start && end <= r) {                     // fully contained
            // Apply now to this node's aggregate; defer the child work
            // by accumulating into lazy[node]. This is the whole reason
            // rangeAdd stays O(log n) instead of O((r - l) log n).
            tree[node] += delta * (long) (end - start + 1);
            lazy[node] += delta;
            return;
        }
        push(node, start, end);                           // partial overlap
        int mid = (start + end) >>> 1;
        rangeAdd(2 * node,     start,   mid, l, r, delta);
        rangeAdd(2 * node + 1, mid + 1, end, l, r, delta);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    private long rangeSum(int node, int start, int end, int l, int r) {
        if (r < start || end < l) {                       // disjoint
            return 0L;
        }
        if (l <= start && end <= r) {                     // fully contained
            return tree[node];
        }
        push(node, start, end);                           // partial overlap
        int mid = (start + end) >>> 1;
        return rangeSum(2 * node,     start,   mid, l, r)
             + rangeSum(2 * node + 1, mid + 1, end, l, r);
    }

    /**
     * Push the pending lazy value of {@code node} down to its two children.
     * After this call, lazy[node] == 0 and the children's tree[] and lazy[]
     * have absorbed the pending delta.
     */
    private void push(int node, int start, int end) {
        long d = lazy[node];
        if (d == 0L) {
            return;
        }
        int mid  = (start + end) >>> 1;
        int lLen = mid - start + 1;
        int rLen = end - mid;
        int lc   = 2 * node;
        int rc   = 2 * node + 1;

        tree[lc] += d * (long) lLen;
        lazy[lc] += d;

        tree[rc] += d * (long) rLen;
        lazy[rc] += d;

        lazy[node] = 0L;
    }

    // --------------------------------------------------------------
    // Validation
    // --------------------------------------------------------------

    private void validateIndex(int idx) {
        if (idx < 0 || idx >= n) {
            throw new IndexOutOfBoundsException(
                "index " + idx + " out of bounds for size " + n);
        }
    }

    private void validateRange(int l, int r) {
        if (l < 0 || r >= n || l > r) {
            throw new IndexOutOfBoundsException(
                "invalid range [" + l + ", " + r + "] for size " + n);
        }
    }
}
