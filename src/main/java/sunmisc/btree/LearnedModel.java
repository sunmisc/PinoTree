package sunmisc.btree;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

public final class LearnedModel {
    private final double slope;
    private final double intercept;
    private final int window;

    public LearnedModel(final double slope, final double intercept, final int window) {
        this.slope = slope;
        this.intercept = intercept;
        this.window = window;
    }

    public static void main(final String[] args) {
        final Random random = new Random();
        final List<Long> keys = LongStream.range(0, 512)
                .map(e -> e + random.nextInt(0, 4096))
                .sorted()
                .distinct()
                .boxed()
                .toList();
        final LearnedModel linearModel = LearnedModel.retrain(keys);
        final int r = new Random().nextInt(0, 4096 + 512);
        final int i = linearModel.search(keys, r);
    }

    public int predict(final long key, final int upperBound) {
        final double raw = Math.fma(this.slope, key, this.intercept);
        final long result = Math.round(raw);
        return Math.clamp(result, 0, upperBound);
    }

    public static LearnedModel retrain(final List<Long> sortedKeys) {
        final int n = sortedKeys.size();
        if (n == 0) {
            return new LearnedModel(0, 0, 1);
        }
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            final double x = sortedKeys.get(i);
            final double y = i;
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        final double denominator = n * sumXX - sumX * sumX;
        final double slope;
        final double intercept;

        if (Math.abs(denominator) < 1e-9) {
            slope = 0;
            intercept = n / 2.0;
        } else {
            slope = (n * sumXY - sumX * sumY) / denominator;
            intercept = (sumY - slope * sumX) / n;
        }
        double totalError = 0;
        for (int i = 0; i < n; i++) {
            final double predicted = slope * sortedKeys.get(i) + intercept;
            totalError += Math.abs(predicted - i);
        }

        final double meanAbsError = totalError / n;
        return new LearnedModel(
                slope,
                intercept,
                Math.max(3, (int)Math.ceil(meanAbsError * 3))
        );
    }

    public int search(final List<Long> sortedKeys, final long key) {
        final int n = sortedKeys.size();
        if (n == 0) {
            return -1;
        }
        final int lastIndex = n - 1;
        final int guess = predict(key, lastIndex);
        final int low = Math.max(0, guess - window);
        final int high = Math.min(lastIndex, guess + window);
        final int index = binarySearch(sortedKeys, key, low, high);
        if (index >= 0) {
            return index;
        }
        final long pivot = sortedKeys.get(-index - 2);
        return key > pivot
                ? binarySearch(sortedKeys, key, high + 1, lastIndex)
                : binarySearch(sortedKeys, key, 0, low - 1);
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