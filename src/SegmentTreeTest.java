import java.time.LocalDate;
import java.util.Random;

/**
 * Lightweight test harness for {@link SegmentTree} and {@link SalesAnalytics}.
 *
 * Rather than pulling in JUnit, this class uses a handful of static assert
 * helpers so the project compiles with a plain {@code javac} and runs with
 * a plain {@code java}. The testing strategy mixes two complementary styles:
 *
 *   1. Explicit edge-case tests — known inputs, known outputs. These are
 *      printed verbosely so the grader can read them.
 *   2. A randomised differential test against a naive O(n) reference. Any
 *      disagreement surfaces bugs that hand-written cases miss — this is
 *      the strongest practical correctness evidence for a Segment Tree
 *      with lazy propagation. Individual assertions in this section are
 *      silent; only a single summary line is printed (plus any failures).
 *
 * Exit code 0 = all pass, 1 = at least one failure.
 *
 * Author: Jiawei Li (25212461), COMP47500 2025/26
 */
public class SegmentTreeTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        section("build + rangeSum");
        testBuildAndSum();

        section("point update");
        testPointUpdate();

        section("range add");
        testRangeAdd();

        section("mixed ops / lazy propagation");
        testMixedLazyPropagation();

        section("edge cases");
        testEdgeCases();

        section("randomised differential (silent)");
        testRandomisedDifferential();

        section("SalesAnalytics end-to-end");
        testSalesAnalyticsEndToEnd();

        System.out.println();
        System.out.println("=== SegmentTreeTest summary ===");
        System.out.println("  passed: " + passed);
        System.out.println("  failed: " + failed);
        if (failed != 0) {
            System.exit(1);
        }
    }

    private static void section(String name) {
        System.out.println();
        System.out.println("--- " + name + " ---");
    }

    // --------------------------------------------------------------
    // Individual test cases
    // --------------------------------------------------------------

    private static void testBuildAndSum() {
        long[] data = { 1, 2, 3, 4, 5 };
        SegmentTree t = new SegmentTree(data);

        assertEquals("sum[0..4]", 15L, t.rangeSum(0, 4));
        assertEquals("sum[0..0]", 1L,  t.rangeSum(0, 0));
        assertEquals("sum[4..4]", 5L,  t.rangeSum(4, 4));
        assertEquals("sum[1..3]", 9L,  t.rangeSum(1, 3));
        assertEquals("size()",    5,   t.size());
    }

    private static void testPointUpdate() {
        long[] data = { 1, 2, 3, 4, 5 };
        SegmentTree t = new SegmentTree(data);

        t.pointUpdate(2, 30);                    // { 1, 2, 30, 4, 5 }
        assertEquals("after point update sum[0..4]", 42L, t.rangeSum(0, 4));
        assertEquals("after point update get(2)",    30L, t.get(2));
        assertEquals("after point update sum[2..4]", 39L, t.rangeSum(2, 4));
    }

    private static void testRangeAdd() {
        long[] data = { 1, 2, 3, 4, 5 };
        SegmentTree t = new SegmentTree(data);

        t.rangeAdd(1, 3, 10);                    // { 1, 12, 13, 14, 5 }
        assertEquals("range add sum[0..4]", 45L, t.rangeSum(0, 4));
        assertEquals("range add sum[1..3]", 39L, t.rangeSum(1, 3));
        assertEquals("range add get(0)",     1L, t.get(0));
        assertEquals("range add get(4)",     5L, t.get(4));
    }

    /**
     * Exercises the lazy-propagation push() path: the per-index queries
     * below only return the correct value if lazy deltas accumulated at
     * ancestor nodes are pushed down correctly.
     */
    private static void testMixedLazyPropagation() {
        long[] data = new long[8];               // all zeros
        SegmentTree t = new SegmentTree(data);

        t.rangeAdd(0, 7, 5);                     // every element = 5
        t.rangeAdd(2, 5, 3);                     // elements 2..5 = 8
        t.pointUpdate(4, 100);                   // element 4 = 100
        t.rangeAdd(3, 6, 1);                     // 3,5,6 += 1; 4 = 101

        long[] expected = { 5, 5, 8, 9, 101, 9, 6, 5 };
        for (int i = 0; i < expected.length; i++) {
            assertEquals("mixed get(" + i + ")", expected[i], t.get(i));
        }
        assertEquals("mixed sum[0..7]", 148L, t.rangeSum(0, 7));
        assertEquals("mixed sum[3..5]", 119L, t.rangeSum(3, 5));
    }

    private static void testEdgeCases() {
        // Size 1
        SegmentTree one = new SegmentTree(new long[] { 42 });
        assertEquals("n=1 sum",    42L, one.rangeSum(0, 0));
        one.rangeAdd(0, 0, 8);
        assertEquals("n=1 rangeAdd", 50L, one.get(0));

        // Out-of-bounds
        SegmentTree t = new SegmentTree(new long[] { 1, 2, 3 });
        assertThrows("negative index",
                     IndexOutOfBoundsException.class,
                     () -> t.get(-1));
        assertThrows("index beyond size",
                     IndexOutOfBoundsException.class,
                     () -> t.get(3));
        assertThrows("reversed range",
                     IndexOutOfBoundsException.class,
                     () -> t.rangeSum(2, 1));

        // Null input
        assertThrows("null data",
                     NullPointerException.class,
                     () -> new SegmentTree(null));

        // Empty input is rejected explicitly (see SegmentTree constructor).
        assertThrows("empty data",
                     IllegalArgumentException.class,
                     () -> new SegmentTree(new long[0]));
    }

    /**
     * Drive random operations through both the Segment Tree and a naive
     * O(n) baseline and cross-check every query. Individual assertions are
     * silent; only a single summary line is printed so the overall test
     * output stays readable.
     */
    private static void testRandomisedDifferential() {
        final int n         = 200;
        final int numOps    = 5_000;
        final long seed     = 0xC0FFEEL;         // fixed so failures reproduce
        Random rng = new Random(seed);

        long[] baseline = new long[n];
        for (int i = 0; i < n; i++) baseline[i] = rng.nextInt(1000);

        SegmentTree tree = new SegmentTree(baseline.clone());

        int assertionsBefore = passed + failed;
        int queriesChecked = 0;

        for (int op = 0; op < numOps; op++) {
            int kind = rng.nextInt(3);
            int l = rng.nextInt(n);
            int r = l + rng.nextInt(n - l);      // l <= r < n
            switch (kind) {
                case 0: {                        // point update
                    int idx = rng.nextInt(n);
                    long val = rng.nextInt(10_000) - 5_000;
                    baseline[idx] = val;
                    tree.pointUpdate(idx, val);
                    break;
                }
                case 1: {                        // range add
                    long delta = rng.nextInt(2_000) - 1_000;
                    for (int i = l; i <= r; i++) baseline[i] += delta;
                    tree.rangeAdd(l, r, delta);
                    break;
                }
                case 2: {                        // range sum query
                    long want = 0;
                    for (int i = l; i <= r; i++) want += baseline[i];
                    long got = tree.rangeSum(l, r);
                    assertEqualsSilent("differential sum[" + l + "," + r + "] @op=" + op,
                                       want, got);
                    queriesChecked++;
                    break;
                }
                default: throw new AssertionError();
            }
        }

        // Final sweep: every single element must match
        for (int i = 0; i < n; i++) {
            assertEqualsSilent("final sweep idx=" + i, baseline[i], tree.get(i));
        }

        int assertionsAfter = passed + failed;
        int total = assertionsAfter - assertionsBefore;
        System.out.printf(
            "  [OK] %d random-op assertions checked (%d range-sum queries, %d final-sweep, n=%d, seed=0x%X)%n",
            total, queriesChecked, n, n, seed);
    }

    private static void testSalesAnalyticsEndToEnd() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        long[] daily = new long[31];             // January
        for (int i = 0; i < 31; i++) daily[i] = 100_00; // €100/day in cents

        SalesAnalytics sa = new SalesAnalytics(start, daily);

        // Baseline: 31 days at €100 each = €3100 = 310000 cents
        assertEquals("Jan revenue",
                     310_000L,
                     sa.totalRevenue(start, start.plusDays(30)));

        // POS correction: Jan 10 was actually €250
        sa.correctSales(start.plusDays(9), 250_00);
        assertEquals("after correction",
                     325_000L,
                     sa.totalRevenue(start, start.plusDays(30)));

        // Supplier rebate: add €5 to every day in Jan 5..15
        sa.applyBulkAdjustment(start.plusDays(4), start.plusDays(14), 5_00);
        // 11 days * 500 cents = 5500 cents added
        assertEquals("after bulk adjustment",
                     330_500L,
                     sa.totalRevenue(start, start.plusDays(30)));

        // Spot-check a single day: Jan 10 was 250_00, then +500 = 255_00
        assertEquals("Jan 10 after rebate",
                     255_00L,
                     sa.getSales(start.plusDays(9)));

        // Date outside window must throw
        assertThrows("date outside window",
                     IllegalArgumentException.class,
                     () -> sa.getSales(start.plusDays(31)));
    }

    // --------------------------------------------------------------
    // Minimal assertion helpers (no external dependencies)
    // --------------------------------------------------------------

    /** Verbose variant: always prints PASS/FAIL. Use for hand-written cases. */
    private static void assertEquals(String name, long expected, long actual) {
        if (expected == actual) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name
                + "  expected=" + expected + " actual=" + actual);
        }
    }

    /** Silent variant: only prints on failure. Use for high-volume loops. */
    private static void assertEqualsSilent(String name, long expected, long actual) {
        if (expected == actual) {
            passed++;
        } else {
            failed++;
            System.out.println("  [FAIL] " + name
                + "  expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertThrows(String name,
                                     Class<? extends Throwable> type,
                                     Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                passed++;
                System.out.println("  [PASS] " + name + "  (threw "
                    + t.getClass().getSimpleName() + ")");
                return;
            }
            failed++;
            System.out.println("  [FAIL] " + name
                + "  expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName());
            return;
        }
        failed++;
        System.out.println("  [FAIL] " + name
            + "  expected " + type.getSimpleName() + " but no exception thrown");
    }
}
