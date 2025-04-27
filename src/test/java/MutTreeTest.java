import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sunmisc.btree.impl.MutBtree;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutTreeTest {
    private static final int MAX_SIZE = 1_000;

    @Test
    public void testAddAndPoll() {
        final TreeMap<Long, String> map = new TreeMap<>();
        final MutBtree bTree = new MutBtree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            map.put(i, i+"");
            bTree.put(i, i+"");
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            final long r = i + MAX_SIZE;
            bTree.put(r, r+"");
            bTree.delete(bTree.first().get().getKey());

            map.put(r, r+"");
            map.pollFirstEntry();
        }
        for (long i = 0; i < 10000; ++i) {
            String expected = map.get(i);
            String actual = bTree.get(i).orElse(null);
            assertEquals(expected, actual,
                    "Key " + i + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    @Test
    public void testAdd() {
        final Map<Long, String> map = new HashMap<>();
        final MutBtree bTree = new MutBtree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            map.put(i, i+"");
            bTree.put(i, i+"");
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            String expected = map.get(i);
            String actual = bTree.get(i).orElse(null);
            assertEquals(expected, actual,
                    "Key " + i + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAddRand(final int step) {
        final Map<Long, String> expectedMap = new HashMap<>();
        final MutBtree actualBTree = new MutBtree();
        final int maxValue = MAX_SIZE;

        // Act - Initial insertion
        for (int i = 0; i < maxValue; i++) {
            final long key = ThreadLocalRandom.current().nextInt(1_000_000);
            expectedMap.put(key, key+"");
            actualBTree.put(key, key+"");
        }
        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            final long keyToRemove = i;
            final long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            try {
                actualBTree.delete(keyToRemove);
            } catch (final Exception ignored) {}

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.put(keyToAdd, keyToAdd+"");
        }
        // Assert
        for (long i = 0; i < maxValue; i++) {
            final long key = i;
            final String expected = expectedMap.get(key);
            final String actual = actualBTree.get(key).orElse(null);
            assertEquals(expected, actual,
                    "Key " + i + " (expected: " + expected + ", actual: " + actual + ")");
        }

    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAdd(final int step) {
        final Map<Long, String> expectedMap = new HashMap<>();
        final MutBtree actualBTree = new MutBtree();
        final int maxValue = MAX_SIZE;

        // Act - Initial insertion
        for (long i = 0; i < maxValue; i++) {
            final long key = i;
            expectedMap.put(key, key+"");
            actualBTree.put(key, key+"");
        }

        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            final long keyToRemove = i;
            final long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            try {
                actualBTree.delete(keyToRemove);
            } catch (Exception ignored) { }

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.put(keyToAdd, keyToAdd+"");
        }
        // Assert
        for (long i = 0; i < maxValue; i++) {
            final long key = i;
            final String expected = expectedMap.get(key);
            final String actual = actualBTree.get(key).orElse(null);
            assertEquals(expected, actual,
                    "Key " + i + " (expected: " + expected + ", actual: " + actual + ")");
        }

    }
    @Test
    public void testMultipleDeletions() {
        final Map<Long, String> map = new HashMap<>();
        final MutBtree bTree = new MutBtree();
        for (long i = 0; i < MAX_SIZE; i++) {
            bTree.put(i, i + "");
            map.put(i, i + "");
        }
        for (long i = 0; i < MAX_SIZE; i += 1) {
            bTree.delete(i);
            map.remove(i);
        }
        for (long i = 0; i < MAX_SIZE >>> 1; i++) {
            assertEquals(map.get(i), bTree.get(i).orElse(null),
                    "Key " + i + " (expected: " + map.get(i) + ", actual: " + bTree.get(i) + ")");
        }
    }
}
