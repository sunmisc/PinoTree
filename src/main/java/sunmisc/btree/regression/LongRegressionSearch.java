package sunmisc.btree.regression;

import java.util.List;

public final class LongRegressionSearch implements RegressionSearch<Long> {
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

    public LongRegressionSearch(final long sumX, final int sumY,
                                final double sumXX, final double sumYY, final double sumXY,
                                final int count,
                                // cached
                                final double slope, final double intercept, final int window) {
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
    public RegressionSearch<Long> addAll(final List<Long> values) {
        long sx = this.sumX;
        final int sy = this.sumY + sumArithmeticProgression(values.size());
        double xx = this.sumXX, yy = this.sumYY, xy = this.sumXY;
        final int c = this.count;
        final int n = values.size();
        for (final long value : values) {
            sx += value;
        }
        final int newCount = c + n;
        final double xbar = sx / (double) newCount;
        final double ybar = sy / (double) newCount;
        for (int i = 0; i < n; i++) {
            final double dx = values.get(i) - xbar;
            final double dy = i - ybar;
            xx += dx * dx;
            yy += dy * dy;
            xy += dx * dy;
        }
         if (c > 0) {
            final double dx = (this.sumX / (double) c) - xbar; // Approximate previous values
            for (int i = 0; i < c; i++) {
                final double dy = i - ybar;
                xx += dx * dx;
                yy += dy * dy;
                xy += dx * dy;
            }
        }
        final double slope = xx != 0 ? xy / xx : 0;
        final double intercept = newCount > 0 ? (sy - slope * sx) / newCount : 0;
        final double errorSquare = Math.max(0, yy - xy * xy / xx);
        final int window = (int) Math.min(
                newCount >>> 3,
                Math.sqrt(errorSquare / newCount)
        );
        return new LongRegressionSearch(sx, sy, xx, yy, xy, newCount, slope, intercept, window);
    }

    @Override
    public RegressionSearch<Long> add(final int index, final Long value) {
        if (this.count == 0) {
            return new LongRegressionSearch(value, index,
                    0, 0, 0,
                    1, 0, 0, 1);
        }
        final int newCount = this.count + 1;
        final double ratio = (double) this.count / newCount;
        final long sx = Math.addExact(this.sumX, value); // Sum of value (x)
        final int sy = this.sumY + index;                // Sum of index (y)
        final double xbar = this.sumX / (double) this.count;      // Mean of value
        final double ybar = this.sumY / (double) this.count;      // Mean of index
        final double dx = value - xbar;             // value as x
        final double dy = index - ybar;             // index as y
        final double xx = this.sumXX + dx * dx * ratio;
        final double yy = this.sumYY + dy * dy * ratio;
        final double xy = this.sumXY + dx * dy * ratio;

        final double slope = xx == 0 ? 0 : xy / xx; // Guard against division-by-zero
        final double intercept = (sy - slope * sx) / newCount;

        final double errorSquare = Math.max(0, yy - xy * xy / xx);
        final int window = (int) Math.min(
                newCount >>> 3,
                Math.sqrt(errorSquare / newCount)
        );
        return new LongRegressionSearch(sx, sy, xx, yy, xy, newCount, slope, intercept, window);
    }

    @Override
    public RegressionSearch<Long> remove(final int index, final Long value) {
        if (this.count == 0) {
            return this;
        }
        final int newCount = this.count - 1;
        final double ratio = (double) this.count / newCount;
        final long sx = Math.subtractExact(this.sumX, value); // Sum of value (x)
        final int sy = this.sumY - index;                    // Sum of index (y)
        final double xbar = this.sumX / (double) this.count;          // Mean of value
        final double ybar = this.sumY / (double) this.count;          // Mean of index
        final double dx = value - xbar;                 // value as x
        final double dy = index - ybar;                 // index as y
        final double xx = this.sumXX - dx * dx * ratio;
        final double yy = this.sumYY - dy * dy * ratio;
        final double xy = this.sumXY - dx * dy * ratio;

        final double slope = xx == 0 ? 0 : xy / xx; // Guard against division-by-zero
        final double intercept = (sy - slope * sx) / newCount;

        final double errorSquare = yy - xy * xy / xx;
        final int window = (int) Math.min(
                newCount >>> 3,
                Math.sqrt(errorSquare / newCount)
        );
        return new LongRegressionSearch(sx, sy, xx, yy, xy, newCount, slope, intercept, window);
    }

    @Override
    public int search(final List<Long> values, final Long key) {
        final int n = values.size();
        if (n == 0) {
            return -1;
        }
        final int lastIndex = n - 1;
        final int guess = this.predict(key, lastIndex);
        final int low = Math.max(0, guess - this.window);
        final int high = Math.min(lastIndex, guess + this.window);
        final int index = binarySearch(values, key, low, high);
        if (index >= 0) {
            return index;
        }
        return key > values.get(guess)
                ? binarySearch(values, key, Math.min(guess + 1, lastIndex), lastIndex)
                : binarySearch(values, key, 0, Math.max(0, guess - 1));
    }

    private int predict(final long key, final int upperBound) {
        final double raw = this.slope * key + this.intercept; // fma
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
