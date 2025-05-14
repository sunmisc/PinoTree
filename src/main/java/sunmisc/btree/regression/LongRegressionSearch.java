package sunmisc.btree.regression;

import java.util.List;

public final class LongRegressionSearch implements RegressionSearch<Long> {
    private static final int THRESHOlD_WINDOW = 2;
    private final long sumX; // Now sums value (Long)
    private final int sumY;  // Now sums index (int)
    private final double sumXX, sumYY, sumXY;
    private final int count;
    private final double slope, intercept;
    private final int window;

    public LongRegressionSearch() {
        this(0, 0, 0, 0,
                0, 0, 0, 0, 0);
    }

    public LongRegressionSearch(long sumX, int sumY,
                                double sumXX, double sumYY, double sumXY,
                                int count,
                                // cached
                                double slope, double intercept, int window) {
        this.sumX = sumX;
        this.sumY = sumY;
        this.sumXX = sumXX;
        this.sumYY = sumYY;
        this.sumXY = sumXY;
        this.count = count;
        this.slope = slope;
        this.intercept = intercept;
        this.window = window;
    }

    @Override
    public RegressionSearch<Long> addAll(List<Long> values) {
        long sx = sumX;
        int sy = sumY + sumArithmeticProgression(values.size());
        double xx = sumXX, yy = sumYY, xy = sumXY;
        int c = count;
        int n = values.size();
        // Accumulate sums
        for (long value : values) {
            sx += value;
        }
        // Update count
        int newCount = c + n;
        // Compute means and sums of squares in one pass
        if (newCount > 0) {
            double xbar = sx / (double) newCount;
            double ybar = sy / (double) newCount;

            // Compute xx, yy, xy using single loop
            for (int i = 0; i < n; i++) {
                double dx = values.get(i) - xbar;
                double dy = i - ybar;
                xx += dx * dx;
                yy += dy * dy;
                xy += dx * dy;
            }
            // Adjust for previous data if count > 0
            if (c > 0) {
                double dx = (sumX / (double) c) - xbar; // Approximate previous values
                for (int i = 0; i < c; i++) {
                    double dy = i - ybar;
                    xx += dx * dx;
                    yy += dy * dy;
                    xy += dx * dy;
                }
            }
        }
        // Compute slope and intercept
        double slope = xx != 0 ? xy / xx : 0;
        double intercept = newCount > 0 ? (sy - slope * sx) / newCount : 0;

        final double errorSquare = Math.max(0, yy - xy * xy / xx);
        final int window = (int) Math.max(
                THRESHOlD_WINDOW,
                Math.sqrt(errorSquare / newCount)
        );
        return new LongRegressionSearch(sx, sy, xx, yy, xy, newCount, slope, intercept, window);
    }

    @Override
    public RegressionSearch<Long> add(int index, Long value) {
        if (count == 0) {
            return new LongRegressionSearch(0, 0, 0, 0, 0, 1, 0, 0, 1);
        }
        final int fact1 = count + 1;
        final double fact2 = (double) count / fact1;
        final long sx = Math.addExact(sumX, value); // Sum of value (x)
        final int sy = sumY + index;                // Sum of index (y)
        final double xbar = sumX / (double) count;      // Mean of value
        final double ybar = sumY / (double) count;      // Mean of index
        final double dx = value - xbar;             // value as x
        final double dy = index - ybar;             // index as y
        final double xx = sumXX + dx * dx * fact2;
        final double yy = sumYY + dy * dy * fact2;
        final double xy = sumXY + dx * dy * fact2;

        final double slope = xx == 0 ? 0 : xy / xx; // Guard against division-by-zero
        final double intercept = (sy - slope * sx) / fact1;

        final double errorSquare = Math.max(0, yy - xy * xy / xx);
        final int window = (int) Math.max(
                THRESHOlD_WINDOW,
                (3 * (Math.sqrt(errorSquare / fact1)))
        );
        return new LongRegressionSearch(sx, sy, xx, yy, xy, fact1, slope, intercept, window);
    }

    @Override
    public RegressionSearch<Long> remove(int index, Long value) {
        if (count == 0) {
            return this;
        }
        final int fact1 = count - 1;
        final double fact2 = (double) count / fact1;
        final long sx = Math.subtractExact(sumX, value); // Sum of value (x)
        final int sy = sumY - index;                    // Sum of index (y)
        final double xbar = sumX / (double) count;          // Mean of value
        final double ybar = sumY / (double) count;          // Mean of index
        final double dx = value - xbar;                 // value as x
        final double dy = index - ybar;                 // index as y
        final double xx = sumXX - dx * dx * fact2;
        final double yy = sumYY - dy * dy * fact2;
        final double xy = sumXY - dx * dy * fact2;

        final double slope = xx == 0 ? 0 : xy / xx; // Guard against division-by-zero
        final double intercept = (sy - slope * sx) / fact1;

        final double errorSquare = Math.max(0, yy - xy * xy / xx);
        final int window = (int) Math.max(
                THRESHOlD_WINDOW,
                (3 * (Math.sqrt(errorSquare / fact1)))
        );
        return new LongRegressionSearch(sx, sy, xx, yy, xy, fact1, slope, intercept, window);
    }

    @Override
    public int search(List<Long> values, Long key) {
        final int n = values.size();
        if (n == 0) {
            return -1;
        }
        final int lastIndex = n - 1;
        final int guess = predict(key, lastIndex);
        final int low = Math.max(0, guess - window);
        final int high = Math.min(lastIndex, guess + window);
        final int index = binarySearch(values, key, low, high);
        if (index >= 0) {
            return index;
        }
        final int pivot = -index - 2;
        if (pivot < 0) {
            return index;
        }
        return key > values.get(guess)
                ? binarySearch(values, key, Math.min(high + 1, lastIndex), lastIndex)
                : binarySearch(values, key, 0, Math.max(0, low - 1));
    }

    private int predict(final long key, final int upperBound) {
        final double raw = slope * key + intercept;
        final long result = Math.round(raw);
        return Math.clamp(result, 0, upperBound);
    }

    private static int binarySearch(final List<Long> list, final long key, final int fromIndex, final int toIndex) {
        int low = fromIndex;
        int high = toIndex;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final long midVal = list.get(mid);
            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -(low + 1);
    }
    private static int sumArithmeticProgression(final int n) {
        final int y = n - 1;
        return (y & 1) == 0 ? n * (y >> 1) : (n >> 1) * y;
    }
}
