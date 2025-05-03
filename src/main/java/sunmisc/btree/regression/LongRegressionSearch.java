package sunmisc.btree.regression;

import sunmisc.btree.impl.LeafNode;

import java.util.*;

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
        double xx = sumXX, yy = sumYY, xy = sumXY;
        long sx = sumX;
        int sy = sumY;
        int c = count;
        for (int index = 0; index < values.size(); index++) {
            final int fact1 = c + 1;
            final long value = values.get(index);
            if (c > 0) {
                final double fact2 = (double) c / fact1;
                final double xbar = sx / (double) c;      // Mean of value
                final double ybar = sy / (double) c;      // Mean of index
                final double dx = value - xbar;             // value as x
                final double dy = index - ybar;             // index as y
                xx += dx * dx * fact2;
                yy += dy * dy * fact2;
                xy += dx * dy * fact2;

            }
            sx += value;
            sy += index;
            c = fact1;
        }
        final double slope = xx == 0 ? 0 : xy / xx; // Guard against division-by-zero
        final double intercept = (sy - slope * sx) / c;

        final double errorSquare = Math.max(0, yy - xy * xy / xx);
        final int window = (int) Math.max(
                THRESHOlD_WINDOW,
                (3 * (Math.sqrt(errorSquare / c)))
        );
        return new LongRegressionSearch(sx, sy, xx, yy, xy, c, slope, intercept, window);
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
        return key > values.get(pivot)
                ? binarySearch(values, key, high + 1, lastIndex)
                : binarySearch(values, key, 0, low - 1);
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
}
