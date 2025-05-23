import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sunmisc.btree.regression.LongRegressionSearch;
import sunmisc.btree.regression.RegressionSearch;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class LinearSearchTest {
    private static final int MAX_SIZE = 100_000;

    @ParameterizedTest
    @ValueSource(ints = {16, 64, 128})
    public void test(final int size) {
        final List<Long> keys = ThreadLocalRandom.current()
                .longs(size, 0, MAX_SIZE)
                .distinct()
                .sorted()
                .boxed()
                .toList();
        final RegressionSearch<Long> model = new LongRegressionSearch().addAll(keys);
        assertIterableEquals(
                keys.stream().map(e -> model.search(keys, e)).toList(),
                keys.stream().map(e -> Collections.binarySearch(keys, e)).toList()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 64, 128})
    public void testRandAcc(final int size) {
        final Random random = ThreadLocalRandom.current();
        final List<Long> keys = LongStream.range(0, MAX_SIZE)
                .map(e -> e + random.nextInt(0, size))
                .distinct()
                .sorted()
                .boxed()
                .toList();
        final RegressionSearch<Long> model = new LongRegressionSearch().addAll(keys);
        assertIterableEquals(
                random.longs(0, MAX_SIZE, MAX_SIZE)
                        .map(e -> model.search(keys, e))
                        .boxed()
                        .toList(),
                random.longs(0, MAX_SIZE, MAX_SIZE)
                        .map(e -> Collections.binarySearch(keys, e))
                        .boxed()
                        .toList()
        );
    }
}
