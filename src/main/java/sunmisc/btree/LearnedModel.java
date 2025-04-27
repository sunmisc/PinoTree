package sunmisc.btree;

import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

public final class LearnedModel {
    private final double slope;
    private final double intercept;
    private final int window;

    private LearnedModel(final double slope, final double intercept, final int window) {
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
        final int i = linearModel.searchEq(keys, r);
    }

    public int predict(final long key, final int upperBound) {
        final double raw = Math.fma(this.slope, key, this.intercept);
        final long result = Math.round(raw);
        return Math.clamp(result, 0, upperBound);
    }

    public static LearnedModel retrain(final List<Long> sortedKeys) {
        final int n = sortedKeys.size();
        if (n == 0) return new LearnedModel(0, 0, 1);

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
            // Все ключи одинаковые — предсказываем центр массива
            slope = 0;
            intercept = n / 2.0;
        } else {
            slope = (n * sumXY - sumX * sumY) / denominator;
            intercept = (sumY - slope * sumX) / n;
        }

        // Вычисление MAE при обучении
        double totalError = 0;
        for (int i = 0; i < n; i++) {
            final double predicted = slope * sortedKeys.get(i) + intercept;
            totalError += Math.abs(predicted - i);
        }

        final double meanAbsError = totalError / n;
        return new LearnedModel(slope, intercept,
                Math.max(3, (int)Math.ceil(meanAbsError * 3)));
    }
    public int searchEq(final List<Long> sortedKeys, final long key) {
        final int n = sortedKeys.size();
        final int guess = this.predict(key, n);
        int low = Math.max(0, guess - this.window);
        int high = Math.min(n - 1, guess + this.window);
        int index = binarySearch(sortedKeys, key, low, high);
        if (index >= 0) {
            return index;
        }
        int step = this.window;
        while (step <= high) {
            final int newLow = Math.max(0, low - step);
            // System.out.println("low range = "+newLow + " " +(newLow - 1));
            index = binarySearch(sortedKeys, key, newLow, low - 1);
            if (index >= 0) {
                return index;
            }
            low = newLow;

            final int newHigh = Math.min(n - 1, high + step);
            //  System.out.println("high range = "+(high + 1) + " " +newHigh);
            index = binarySearch(sortedKeys, key, high + 1, newHigh);
            if (index >= 0) {
                return index;
            }
            high = newHigh;

            // Удваиваем шаг
            step <<= 1;
        }

        return -1;
    }

    // Вспомогательный метод бинарного поиска
    private static int binarySearch(final List<Long> list, final long key, final int fromIndex, final int toIndex) {
        int low = fromIndex;
        int high = toIndex;
        while (low <= high) {
            final int mid = (low + high) >>> 1; // Безопасное вычисление середины
            final long midVal = list.get(mid);
            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid; // Ключ найден
            }
        }
        return -(low + 1);
    }
    public static boolean learned = true;
    public int search(final List<Long> sortedKeys, final long key) {
        if (learned) {
            if (sortedKeys.isEmpty()) {
                return -1;
            }
            final int guess = this.predict(key, sortedKeys.size());
            int low = Math.max(0, guess - this.window);
            int high = Math.min(guess + this.window, sortedKeys.size() - 1);

            while (low <= high) {
                final int mid = (low + high) >>> 1;
                final long midVal = sortedKeys.get(mid);
                if (midVal < key) {
                    low = mid + 1;
                } else if (midVal > key) {
                    high = mid - 1;
                } else {
                    return mid;
                }
            }
        }
        return this.binSearch(sortedKeys, key);
    }

    public int binSearch(final List<Long> list, final long key) {
        return binarySearch(list, key, 0, list.size() - 1);
    }
}