import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sunmisc.btree.impl.BTree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test {
    private static final int MAX_SIZE = 1_000;

    @org.junit.jupiter.api.Test
    public void testAddAndPoll() throws IOException {
        final TreeMap<Long, String> map = new TreeMap<>();
        final BTree bTree = new BTree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            map.put(i, i+"");
            bTree.insert(i, i+"");
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            final long r = i + MAX_SIZE;
            bTree.insert(r, r+"");
            bTree.delete(bTree.firstEntry().key());

            map.put(r, r+"");
            map.pollFirstEntry();
        }
        for (long i = 0; i < 10000; ++i) {
            if (!Objects.equals(map.get(i), bTree.search(i))) {
                throw new IllegalStateException();
            }
        }
    }

    @org.junit.jupiter.api.Test
    public void testAdd() throws IOException {
        final Map<Long, String> map = new HashMap<>();
        final BTree bTree = new BTree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            map.put(i, i+"");
            bTree.insert(i, i+"");
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            if (!Objects.equals(map.get(i), bTree.search(i))) {
                throw new IllegalStateException();
            }
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAddRand(final int step) throws IOException {
        final Map<Long, String> expectedMap = new HashMap<>();
        final BTree actualBTree = new BTree();
        final int maxValue = MAX_SIZE;

        // Act - Initial insertion
        for (int i = 0; i < maxValue; i++) {
            final long key = ThreadLocalRandom.current().nextInt(1_000_000);
            expectedMap.put(key, key+"");
            actualBTree.insert(key, key+"");
        }

        actualBTree.print();
        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            final long keyToRemove = i;
            final long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            actualBTree.delete(keyToRemove);

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.insert(keyToAdd, keyToAdd+"");
        }
        // Assert
        for (long i = 0; i < maxValue; i++) {
            final long key = i;
            final String expected = expectedMap.get(key);
            final String actual = actualBTree.search(key);
            if (!Objects.equals(expected, actual)) {
                actualBTree.print();
            }
            assertEquals(expected, actual, "Key " + key);
        }

    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAdd(final int step) throws IOException {
        final Map<Long, String> expectedMap = new HashMap<>();
        final BTree actualBTree = new BTree();
        final int maxValue = MAX_SIZE;

        // Act - Initial insertion
        for (long i = 0; i < maxValue; i++) {
            final long key = i;
            expectedMap.put(key, key+"");
            actualBTree.insert(key, key+"");
        }

        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            final long keyToRemove = i;
            final long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            actualBTree.delete(keyToRemove);

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.insert(keyToAdd, keyToAdd+"");
        }
        actualBTree.print();
        // Assert
        for (long i = 0; i < maxValue; i++) {
            final long key = i;
            final String expected = expectedMap.get(key);
            final String actual = actualBTree.search(key);
            if (!Objects.equals(expected, actual)) {
                actualBTree.print();
            }
            assertEquals(expected, actual, "Key " + key);
        }

    }
    @org.junit.jupiter.api.Test
    public void testMultipleDeletions() throws IOException {
        final Map<Long, String> map = new HashMap<>();
        final BTree bTree = new BTree();
        for (long i = 0; i < MAX_SIZE; i++) {
            bTree.insert(i, i + "");
            map.put(i, i + "");
        }
        for (long i = 0; i < MAX_SIZE; i += 1) {
            bTree.delete(i);
            map.remove(i);
        }
        for (long i = 0; i < MAX_SIZE >>> 1; i++) {
            assertEquals(map.get(i), bTree.search(i), "Key " + i);
        }
    }
}
