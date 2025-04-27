import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sunmisc.btree.LearnedModel;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinearSearchTest {
    @ParameterizedTest
    @ValueSource(ints = {16, 64, 128})
    public void test(int size) {
        List<Long> keys = ThreadLocalRandom.current()
                .longs(size, 0, 100_000)
                .distinct()
                .sorted()
                .boxed()
                .toList();
        LearnedModel model = LearnedModel.retrain(keys);
        for (long k : keys) {
            int expected = model.searchEq(keys, k);
            int actual = Collections.binarySearch(keys, k);
            assertEquals(expected, actual, "Key " + k +
                    " expected " + expected + " actual " + actual);
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {16, 64, 128})
    public void testRandAcc(int size) {
        long seed = ThreadLocalRandom.current().nextLong();
        List<Long> keys = LongStream.range(0, 100_000)
                .map(e -> e +
                        new Random(seed).nextInt(0, size))
                .distinct()
                .sorted()
                .boxed()
                .toList();
        LearnedModel model = LearnedModel.retrain(keys);
        for (long k : keys) {
            int expected = model.searchEq(keys, k);
            int actual = Collections.binarySearch(keys, k);
            assertEquals(expected, actual, "Key " + k +
                    " expected " + expected + " actual " + actual);
        }
    }

}
