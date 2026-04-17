import java.util.Random;

/**
 * Compares {@link SegmentTree} against a naive O(n)-per-operation baseline
 * (plain long[] with linear range sums and range adds).
 *
 * This is not JMH-grade micro-benchmarking, but it is sufficient to
 * illustrate the expected asymptotic gap for the technical report. A
 * JIT-warmup pass is included before timing to reduce variance, and both
 * implementations are driven with an identical pre-generated operation
 * stream so the comparison is apples-to-apples.
 *
 * By default the benchmark sweeps a range of input sizes (1k, 10k, 50k,
 * 100k) and prints a single summary table. To run a single custom size,
 * pass explicit arguments.
 *
 * Sample invocations:
 *   java -cp out PerformanceBenchmark
 *   java -cp out PerformanceBenchmark 200000 100000 42
 *
 * Arguments when provided: n numOps seed
 *
 * Author: Jiawei Li (25212461), COMP47500 2025/26
 */
public class PerformanceBenchmark {

    private static final int[] DEFAULT_SIZES = { 1_000, 10_000, 50_000, 100_000 };
    private static final int   OPS_PER_ELEMENT = 1;     // numOps scales with n
    private static final long  DEFAULT_SEED    = 42L;

    public static void main(String[] args) {
        if (args.length == 0) {
            runSweep(DEFAULT_SIZES, DEFAULT_SEED);
        } else {
            int  n      = Integer.parseInt(args[0]);
            int  numOps = args.length > 1 ? Integer.parseInt(args[1]) : n;
            long seed   = args.length > 2 ? Long.parseLong(args[2])   : DEFAULT_SEED;
            runSingle(n, numOps, seed);
        }
    }

    // --------------------------------------------------------------
    // Multi-size sweep
    // --------------------------------------------------------------

    private static void runSweep(int[] sizes, long seed) {
        System.out.println("Segment Tree vs naive array — benchmark sweep");
        System.out.println("  seed = " + seed);
        System.out.println();

        // Header
        System.out.printf("  %-10s %-10s %14s %14s %12s %10s%n",
            "n", "numOps", "naive (ms)", "segtree (ms)", "speed-up", "status");
        System.out.println("  " + "-".repeat(74));

        double totalSpeedup = 0.0;
        int runs = 0;
        boolean anyDisagree = false;

        for (int n : sizes) {
            int numOps = n * OPS_PER_ELEMENT;
            Result r = runOne(n, numOps, seed);

            String status = r.checksumsMatch ? "OK" : "MISMATCH";
            System.out.printf("  %-10d %-10d %14.2f %14.2f %11.2fx %10s%n",
                n, numOps, r.naiveMs, r.segMs, r.speedup, status);

            if (!r.checksumsMatch) anyDisagree = true;
            totalSpeedup += r.speedup;
            runs++;
        }

        System.out.println();
        double avg = totalSpeedup / runs;
        System.out.printf("  Average speed-up across %d sizes : %.2fx%n", runs, avg);
        System.out.println();
        System.out.println("  Interpretation:");
        System.out.println("    The segment tree answers range-sum and range-add in O(log n),");
        System.out.println("    while the naive array needs O(r - l) per such operation. As n");
        System.out.println("    grows, the gap widens in line with that asymptotic difference.");
        System.out.println("    Matching checksums confirm both implementations compute the");
        System.out.println("    same result for every query.");

        if (anyDisagree) {
            System.out.println();
            System.out.println("  [WARN] at least one size produced mismatched checksums.");
            System.exit(1);
        }
    }

    // --------------------------------------------------------------
    // Single-size mode (preserved from the original benchmark)
    // --------------------------------------------------------------

    private static void runSingle(int n, int numOps, long seed) {
        System.out.println("Segment Tree vs naive array");
        System.out.println("  n       = " + n);
        System.out.println("  numOps  = " + numOps);
        System.out.println("  seed    = " + seed);
        System.out.println();

        Result r = runOne(n, numOps, seed);

        System.out.printf("  naive     : %10.2f ms  (checksum %d)%n",
            r.naiveMs, r.naiveChecksum);
        System.out.printf("  segtree   : %10.2f ms  (checksum %d)%n",
            r.segMs, r.segChecksum);
        System.out.printf("  speed-up  : %10.2fx%n", r.speedup);

        if (!r.checksumsMatch) {
            System.out.println();
            System.out.println("  [WARN] checksums disagree - implementations diverge!");
            System.exit(1);
        }
    }

    // --------------------------------------------------------------
    // Core timed run (shared by both modes)
    // --------------------------------------------------------------

    /** Container for a single (n, numOps) measurement. */
    private static final class Result {
        final double naiveMs;
        final double segMs;
        final double speedup;
        final long   naiveChecksum;
        final long   segChecksum;
        final boolean checksumsMatch;

        Result(double naiveMs, double segMs, long naiveChecksum, long segChecksum) {
            this.naiveMs        = naiveMs;
            this.segMs          = segMs;
            this.speedup        = naiveMs / segMs;
            this.naiveChecksum  = naiveChecksum;
            this.segChecksum    = segChecksum;
            this.checksumsMatch = naiveChecksum == segChecksum;
        }
    }

    /**
     * Build a shared random input and operation stream, run both
     * implementations once for JIT warm-up, then run both again with
     * timing. The same operation stream is replayed in each run so the
     * comparison is fair.
     */
    private static Result runOne(int n, int numOps, long seed) {
        long[] initial = new long[n];
        Random rng = new Random(seed);
        for (int i = 0; i < n; i++) initial[i] = rng.nextInt(1000);

        int[]  kinds = new int [numOps];
        int[]  ls    = new int [numOps];
        int[]  rs    = new int [numOps];
        long[] vals  = new long[numOps];
        for (int op = 0; op < numOps; op++) {
            kinds[op] = rng.nextInt(3);
            int l = rng.nextInt(n);
            int r = l + rng.nextInt(n - l);
            ls[op]   = l;
            rs[op]   = r;
            vals[op] = rng.nextInt(2000) - 1000;
        }

        // Warm-up (helps the JIT produce stable numbers). Discard results.
        blackhole(runNaive     (initial.clone(), kinds, ls, rs, vals));
        blackhole(runSegmentTree(initial.clone(), kinds, ls, rs, vals));

        long t0 = System.nanoTime();
        long naiveChecksum = runNaive(initial.clone(), kinds, ls, rs, vals);
        long naiveNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        long segChecksum = runSegmentTree(initial.clone(), kinds, ls, rs, vals);
        long segNs = System.nanoTime() - t0;

        return new Result(naiveNs / 1e6, segNs / 1e6, naiveChecksum, segChecksum);
    }

    /** Naive O(n) reference. Returns a checksum to prevent dead-code removal. */
    private static long runNaive(long[] arr,
                                 int[] kinds, int[] ls, int[] rs, long[] vals) {
        long checksum = 0;
        for (int op = 0; op < kinds.length; op++) {
            int l = ls[op], r = rs[op];
            switch (kinds[op]) {
                case 0:                              // point update
                    arr[l] = vals[op];
                    break;
                case 1:                              // range add
                    for (int i = l; i <= r; i++) arr[i] += vals[op];
                    break;
                case 2:                              // range sum
                    long s = 0;
                    for (int i = l; i <= r; i++) s += arr[i];
                    checksum ^= s;
                    break;
            }
        }
        return checksum;
    }

    private static long runSegmentTree(long[] arr,
                                       int[] kinds, int[] ls, int[] rs, long[] vals) {
        SegmentTree t = new SegmentTree(arr);
        long checksum = 0;
        for (int op = 0; op < kinds.length; op++) {
            int l = ls[op], r = rs[op];
            switch (kinds[op]) {
                case 0:  t.pointUpdate(l, vals[op]);     break;
                case 1:  t.rangeAdd   (l, r, vals[op]);  break;
                case 2:  checksum ^= t.rangeSum(l, r);   break;
            }
        }
        return checksum;
    }

    /** Prevent the JIT from dead-code-eliminating a discarded result. */
    private static long BLACKHOLE = 0;
    private static void blackhole(long v) { BLACKHOLE ^= v; }
}
