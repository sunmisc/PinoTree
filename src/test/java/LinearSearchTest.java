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

public class LinearSearchTest {
    @ParameterizedTest
    @ValueSource(ints = {16, 64, 128})
    public void test(final int size) {
        final List<Long> keys = ThreadLocalRandom.current()
                .longs(size, 0, 100_000)
                .distinct()
                .sorted()
                .boxed()
                .toList();
        final RegressionSearch<Long> model = new LongRegressionSearch().addAll(keys);
        for (final long k : keys) {
            final int expected = model.search(keys, k);
            final int actual = Collections.binarySearch(keys, k);
            assertEquals(expected, actual, "Key " + k +
                    " expected " + expected + " actual " + actual);
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {16, 64, 128})
    public void testRandAcc(final int size) {
        final long seed = ThreadLocalRandom.current().nextLong();
        final List<Long> keys = LongStream.range(0, 100_000)
                .map(e -> e +
                        new Random(seed).nextInt(0, size))
                .distinct()
                .sorted()
                .boxed()
                .toList();
        final RegressionSearch<Long> model = new LongRegressionSearch().addAll(keys);
        for (int i = 0; i < keys.size(); i++) {
            final long k = ThreadLocalRandom.current().nextLong(0, 100_000);
            final int expected = model.search(keys, k);
            final int actual = Collections.binarySearch(keys, k);
            assertEquals(expected, actual, "Key " + k +
                    " expected " + expected + " actual " + actual);
        }
    }

}
