import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Retail Sales Analytics — the real-world scenario chosen for COMP47500.
 *
 *   A retail chain records daily revenue over a fixed window (e.g. one year).
 *   The analytics engine must support the following operations efficiently,
 *   even as historical figures are corrected and bulk adjustments are applied:
 *
 *     - getSales(day)                   point query    O(log n)
 *     - correctSales(day, newAmount)    point update   O(log n)
 *     - totalRevenue(from, to)          range sum      O(log n)
 *     - applyBulkAdjustment(from, to,d) range add      O(log n) via lazy prop.
 *
 * A naive array implementation would take O(n) per range-sum or bulk
 * adjustment. With a Segment Tree that supports lazy propagation, each of
 * these drops to O(log n) — a meaningful difference once n grows from 365
 * days to tens of thousands of hourly or per-store buckets.
 *
 * Why this wrapper exists:
 *   {@link SegmentTree} is intentionally a plain integer-indexed data
 *   structure with no domain concepts. This class is a thin facade that
 *   translates domain inputs (calendar dates, monetary amounts) into
 *   tree indices. Keeping these responsibilities separate means the same
 *   {@link SegmentTree} can be reused for other problems (temperature
 *   sensors, leaderboards, ...) without dragging retail-specific concerns
 *   into the core algorithm.
 *
 * Monetary values are stored as cents (long) to avoid double-precision drift.
 *
 * Author: Jiawei Li (25212461), COMP47500 2025/26
 */
public class SalesAnalytics {

    private final LocalDate firstDay;
    private final int numDays;
    private final SegmentTree tree;

    /**
     * @param firstDay   the date corresponding to day index 0
     * @param dailyCents initial daily sales in cents, length == numDays
     */
    public SalesAnalytics(LocalDate firstDay, long[] dailyCents) {
        if (firstDay == null) {
            throw new NullPointerException("firstDay must not be null");
        }
        if (dailyCents == null) {
            throw new NullPointerException("dailyCents must not be null");
        }
        this.firstDay = firstDay;
        this.numDays  = dailyCents.length;
        this.tree     = new SegmentTree(dailyCents);
    }

    // --------------------------------------------------------------
    // Domain API
    // --------------------------------------------------------------

    /** @return sales on the given date, in cents. */
    public long getSales(LocalDate day) {
        return tree.get(dayIndex(day));
    }

    /** Overwrite the sales total for a given day (e.g. POS correction). */
    public void correctSales(LocalDate day, long newAmountCents) {
        tree.pointUpdate(dayIndex(day), newAmountCents);
    }

    /**
     * @return total revenue over the inclusive date range [from, to].
     */
    public long totalRevenue(LocalDate from, LocalDate to) {
        return tree.rangeSum(dayIndex(from), dayIndex(to));
    }

    /**
     * Apply a flat per-day adjustment (positive or negative) to every day
     * in the inclusive range [from, to]. Example: a supplier rebate worth
     * 50c/day is backdated to every day in Q1.
     */
    public void applyBulkAdjustment(LocalDate from, LocalDate to, long deltaCents) {
        tree.rangeAdd(dayIndex(from), dayIndex(to), deltaCents);
    }

    /** @return number of days covered by this analytics window. */
    public int numDays() {
        return numDays;
    }

    // --------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------

    /** Translate a calendar date into the zero-based internal index. */
    private int dayIndex(LocalDate day) {
        if (day == null) {
            throw new NullPointerException("day must not be null");
        }
        long offset = ChronoUnit.DAYS.between(firstDay, day);
        if (offset < 0 || offset >= numDays) {
            throw new IllegalArgumentException(
                "date " + day + " is outside the analytics window starting "
                + firstDay + " (" + numDays + " days)");
        }
        return (int) offset;
    }
}
