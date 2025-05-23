import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sunmisc.btree.impl.MutBtree;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class MutTreeTest {
    private static final int MAX_SIZE = 10_000;

    @Test
    public void testAddAndPoll() {
        final SortedMap<Long, String> map = new TreeMap<>();
        final MutBtree btree = new MutBtree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            map.put(i, String.valueOf(i));
            btree.put(i, String.valueOf(i));
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            final long key = i + MAX_SIZE;
            btree.put(key, String.valueOf(key));
            btree.delete(btree.first().get().getKey());

            map.put(key, String.valueOf(key));
            map.pollFirstEntry();
        }
        assertIterableEquals(
                map.entrySet(),
                btree.stream().toList()
        );
    }

    @Test
    public void testAdd() {
        final SortedMap<Long, String> map = new TreeMap<>();
        final MutBtree btree = new MutBtree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            map.put(i, String.valueOf(i));
            btree.put(i, String.valueOf(i));
        }
        assertIterableEquals(
                map.entrySet(),
                btree.stream().toList()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAddRand(final int step) {
        final SortedMap<Long, String> map = new TreeMap<>();
        final MutBtree btree = new MutBtree();
        final int maxValue = MAX_SIZE;
        for (int i = 0; i < maxValue; i++) {
            final long key = ThreadLocalRandom.current().nextInt(1_000_000);
            map.put(key, String.valueOf(key));
            btree.put(key, String.valueOf(key));
        }
        for (long i = 0; i < maxValue; i += step) {
            final long keyToAdd = i + step;
            try {
                btree.delete(i);
                map.remove(i);
            } catch (final Exception ignored) {

            }
            map.put(keyToAdd, String.valueOf(keyToAdd));
            btree.put(keyToAdd, String.valueOf(keyToAdd));
        }
        assertIterableEquals(
                map.entrySet(),
                btree.stream().toList()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAdd(final int step) {
        final SortedMap<Long, String> map = new TreeMap<>();
        final MutBtree btree = new MutBtree();
        final int maxValue = MAX_SIZE;
        for (long i = 0; i < maxValue; i++) {
            map.put(i, String.valueOf(i));
            btree.put(i, String.valueOf(i));
        }
        for (long i = 0; i < maxValue; i += step) {
            final long keyToAdd = i + step;
            map.remove(i);
            try {
                btree.delete(i);
            } catch (final Exception ignored) {

            }
            map.put(keyToAdd, String.valueOf(keyToAdd));
            btree.put(keyToAdd, String.valueOf(keyToAdd));
        }
        assertIterableEquals(
                map.entrySet(),
                btree.stream().toList()
        );
    }

    @Test
    public void testMultipleDeletions() {
        final SortedMap<Long, String> map = new TreeMap<>();
        final MutBtree btree = new MutBtree();
        for (long i = 0; i < MAX_SIZE; i++) {
            btree.put(i, String.valueOf(i));
            map.put(i, String.valueOf(i));
        }
        for (long i = 0; i < MAX_SIZE; i += 1) {
            btree.delete(i);
            map.remove(i);
        }
        assertIterableEquals(
                map.entrySet(),
                btree.stream().toList()
        );
    }
}
