package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.objects.OEntry;
import sunmisc.btree.regression.LongRegressionSearch;
import sunmisc.btree.regression.RegressionSearch;

import java.util.*;

public final class LeafNode extends AbstractNode {
    private final RegressionSearch<Long> longRegressionSearch;
    private final List<Entry> addresses;

    public LeafNode(final Table table, final List<Entry> keys) {
        this(table, keys, new LongRegressionSearch().addAll(new KeyListWrapper(keys)));
    }

    public LeafNode(final Table table,
                    final List<Entry> keys,
                    final RegressionSearch<Long> regression) {
        super(table, List.of());
        this.addresses = keys;
        this.longRegressionSearch = regression;
    }

    @Override
    public int size() {
        return this.addresses.size();
    }

    @Override
    public IndexedNode withoutFirst() {
        RegressionSearch<Long> regression = this.longRegressionSearch;
        if (!this.addresses.isEmpty()) {
            final long val = this.addresses.getFirst().key();
            regression = regression.remove(0, val);
        }
        return this.createNewNode(Utils.withoutFirst(this.addresses), regression);
    }

    private IndexedNode head() {
        final int last = this.addresses.size() - 1;
        RegressionSearch<Long> regression = this.longRegressionSearch;
        if (last >= 0) {
            final long val = this.addresses.get(last).key();
            regression = regression.remove(last, val);
        }
        return this.createNewNode(Utils.withoutLast(this.addresses), regression);
    }

    private IndexedNode createNewNode(final List<Entry> keys, final RegressionSearch<Long> regression) {
        final Node leaf = new LeafNode(this.table, keys, regression);
        return new LazyNode(() -> leaf, this.table.nodes().put(leaf));
    }

    private IndexedNode createNewNode(final List<Entry> keys) {
        final Node leaf = new LeafNode(this.table, keys);
        return new LazyNode(() -> leaf, this.table.nodes().put(leaf));
    }

    @Override
    public IndexedNode delete(final long key) {
        final int idx = this.longRegressionSearch.search(this.keys(), key);
        if (idx < 0) {
            throw new IllegalArgumentException();
        }
        final long val = this.addresses.get(idx).key();
        return this.createNewNode(
                Utils.withoutIdx(idx, this.addresses),
                this.longRegressionSearch.remove(idx, val)
        );
    }

    @Override
    public IndexedNode delete(final long key, final String value) {
        final int idx = this.longRegressionSearch.search(this.keys(), key);
        if (idx < 0) {
            throw new NoSuchElementException("No entry found for key: " + key);
        }
        final Entry entry = this.addresses.get(idx);
        final long val = entry.key();
        if (entry.value().value().equals(value)) {
            return this.createNewNode(
                    Utils.withoutIdx(idx, this.addresses),
                    this.longRegressionSearch.remove(idx, val)
            );
        }
        throw new IllegalArgumentException(
                "Value mismatch for key " + key + ": expected '" + value + "', found '" + entry.value().value() + "'"
        );
    }

    @Override
    public IndexedNode merge(final Node other) {
        if (!other.isLeaf()) {
            throw new IllegalArgumentException("Can only merge with another Leaf node");
        }
        final List<Entry> concat = new ArrayList<>(this.addresses);
        other.forEach(concat::add);
        return this.createNewNode(concat);
    }

    @Override
    public List<Long> keys() {
        return new KeyListWrapper(this.addresses);
    }
    @Override
    public int minChildren() {
        return LEAF_MIN_CHILDREN;
    }

    @Override
    public int maxChildren() {
        return LEAF_MAX_CHILDREN;
    }

    @Override
    public Split insert(final long key, final String value) {
        int idx = this.longRegressionSearch.search(this.keys(), key);

        final Entry entry = new OEntry(key, value, this.table.values().put(Map.entry(key, value)));

        final List<Entry> newKeys;
        RegressionSearch<Long> newModel = this.longRegressionSearch;
        if (idx < 0) {
            idx = -idx - 1;
            newKeys = Utils.append(idx, entry, this.addresses);
            newModel = this.longRegressionSearch.add(idx, key);
        } else {
            newKeys = Utils.set(idx, entry, this.addresses);
        }
        final LeafNode newLeaf = new LeafNode(this.table, newKeys, newModel);
        return newLeaf.shouldSplit()
                ? newLeaf.split()
                : new Split.UnarySplit(
                        new LazyNode(
                                () -> newLeaf,
                                this.table.nodes().put(newLeaf)
                        )
        );
    }

    public Split split() {
        final int cutoff = this.addresses.size() >>> 1;
        final long mid = this.addresses.get(cutoff).key();

        final List<List<Entry>> keyPair = Utils.splitAt(cutoff, this.addresses);
        final List<Entry> thisKeys = keyPair.get(0);
        final List<Entry> otherKeys = keyPair.get(1);

        final IndexedNode other = this.createNewNode(otherKeys);
        final IndexedNode thisSplit = this.createNewNode(thisKeys);
        return new Split.RebalancedSplit(mid, thisSplit, other);
    }

    @Override
    public Optional<Entry> firstEntry() {
        return this.addresses.isEmpty() ? Optional.empty() : Optional.of(this.addresses.getFirst());
    }

    @Override
    public Optional<Entry> lastEntry() {
        return this.addresses.isEmpty() ? Optional.empty() : Optional.of(this.addresses.getLast());
    }

    @Override
    public List<IndexedNode> stealFirstKeyFrom(final Node right) {
        final Entry stolenKey = right.firstEntry().orElseThrow();

        final List<Entry> newKeys = Utils.append(
                this.addresses.size(), stolenKey, this.addresses);

        return List.of(
                this.createNewNode(newKeys,
                        this.longRegressionSearch.add(this.addresses.size(), stolenKey.key())),
                right.withoutFirst()
        );
    }

    @Override
    public List<IndexedNode> giveLastKeyTo(final Node right) {
        final Entry keyToGive = this.addresses.getLast();

        final List<Entry> rightKeys = new ArrayList<>(
                right.size() + 1);
        rightKeys.add(keyToGive);
        right.forEach(rightKeys::add);

        return List.of(this.head(), this.createNewNode(rightKeys));
    }

    @Override
    public Optional<String> search(final long key) {
        final int idx = this.longRegressionSearch.search(this.keys(), key);
        return idx < 0
                ? Optional.empty()
                : Optional.of(this.addresses.get(idx).value()).map(ValueLocation::value);
    }

    @Override
    public SequencedMap<Long, String> rangeSearch(final long minKey, final long maxKey) {
        final SequencedMap<Long, String> result = new LinkedHashMap<>();
        System.out.println(this.keys());
        for (final Entry entry : this.addresses) {
            final long key = entry.key();
            if (key >= minKey && key <= maxKey) {
                result.put(key, entry.value().value());
            }
        }
        return result;
    }

    @Override
    public Iterator<Entry> iterator() {
        return this.addresses.iterator();
    }

    private static final class KeyListWrapper extends AbstractList<Long> {
        private final List<Entry> entries;

        public KeyListWrapper(final List<Entry> entries) {
            this.entries = entries;
        }

        @Override
        public Long get(final int index) {
            return this.entries.get(index).key();
        }

        @Override
        public int size() {
            return this.entries.size();
        }
    }
}