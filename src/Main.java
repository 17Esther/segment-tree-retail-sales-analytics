import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

/**
 * Narrative demo of the Retail Sales Analytics scenario. Readable when run
 * from the command line; intended to show the grader how the Segment Tree
 * behaves against a realistic data flow.
 *
 * Run with:
 *   javac -d out src/*.java
 *   java -cp out Main
 *
 * Author: Jiawei Li (25212461), COMP47500 2025/26
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println(" Retail Sales Analytics — Segment Tree Demo");
        System.out.println("===========================================");
        System.out.println();

        // --- Generate a year of plausible daily sales -----------------
        LocalDate firstDay = LocalDate.of(2025, 1, 1);
        int days = 365;
        long[] daily = synthesiseYear(days, /*seed=*/ 2025L);

        SalesAnalytics shop = new SalesAnalytics(firstDay, daily);
        System.out.println("Loaded " + days + " days of daily revenue starting "
            + firstDay + ".");

        // --- Query: Q1 total revenue ----------------------------------
        LocalDate q1End = LocalDate.of(2025, Month.MARCH, 31);
        System.out.printf(
            "%nQ1 revenue (%s to %s) : €%,.2f%n",
            firstDay, q1End, euros(shop.totalRevenue(firstDay, q1End)));

        // --- Correction: an accountant flags that Jan 15 was wrong ----
        LocalDate jan15 = LocalDate.of(2025, Month.JANUARY, 15);
        long before = shop.getSales(jan15);
        long corrected = before + 50_00;                  // add €50
        shop.correctSales(jan15, corrected);
        System.out.printf("POS correction: %s  €%,.2f → €%,.2f%n",
            jan15, euros(before), euros(corrected));

        // --- Bulk adjustment: supplier rebate over Jan ----------------
        LocalDate janEnd = LocalDate.of(2025, Month.JANUARY, 31);
        shop.applyBulkAdjustment(firstDay, janEnd, 1_00); // +€1/day in Jan
        System.out.println("Bulk adjustment: +€1.00/day applied to all 31 days of January.");

        // --- Re-query Q1 ----------------------------------------------
        System.out.printf("Q1 revenue after adjustments : €%,.2f%n",
            euros(shop.totalRevenue(firstDay, q1End)));

        // --- A few weekly queries -------------------------------------
        System.out.println();
        System.out.println("Sample weekly roll-ups:");
        for (int week = 0; week < 4; week++) {
            LocalDate wStart = firstDay.plusDays(week * 7L);
            LocalDate wEnd   = wStart.plusDays(6);
            System.out.printf("  week %d (%s to %s) : €%,.2f%n",
                week + 1, wStart, wEnd,
                euros(shop.totalRevenue(wStart, wEnd)));
        }

        // --- Wrap up ---------------------------------------------------
        System.out.println();
        System.out.println("Done. Run SegmentTreeTest for correctness tests,");
        System.out.println("or PerformanceBenchmark for timing evidence.");
    }

    /** Synthesise plausible daily revenue (cents) with weekly + seasonal wobble. */
    private static long[] synthesiseYear(int days, long seed) {
        Random rng = new Random(seed);
        long[] out = new long[days];
        for (int d = 0; d < days; d++) {
            double season = 1.0 + 0.30 * Math.sin(2 * Math.PI * d / 365.0);
            double weekly = 1.0 + 0.15 * Math.sin(2 * Math.PI * d /   7.0);
            double noise  = 0.85 + 0.30 * rng.nextDouble();
            double euros  = 1_500.0 * season * weekly * noise;
            out[d] = Math.round(euros * 100.0);           // cents
        }
        return out;
    }

    /** Cents → euros as a double, for display only. */
    private static double euros(long cents) {
        return cents / 100.0;
    }
}
