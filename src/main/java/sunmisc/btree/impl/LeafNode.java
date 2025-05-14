package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.objects.OEntry;
import sunmisc.btree.objects.Table;
import sunmisc.btree.regression.LongRegressionSearch;
import sunmisc.btree.regression.RegressionSearch;

import java.util.*;
import java.util.function.Consumer;

import static sunmisc.btree.impl.Constants.LEAF_MAX_CHILDREN;
import static sunmisc.btree.impl.Constants.LEAF_MIN_CHILDREN;

public final class LeafNode extends AbstractNode {
    private final RegressionSearch<Long> search;
    private final List<Entry> addresses;

    public LeafNode(final Table table, final List<Entry> keys) {
        this(table, keys, new LongRegressionSearch().addAll(new KeyListWrapper(keys)));
    }

    public LeafNode(final Table table, final List<Entry> keys, RegressionSearch<Long> search) {
        super(table, new ArrayList<>());
        this.addresses = keys;
        this.search = new LongRegressionSearch().addAll(new KeyListWrapper(keys));
    }

    @Override
    public int size() {
        return this.addresses.size();
    }

    @Override
    public IndexedNode tail() {
        RegressionSearch<Long> regression = search;
        if (!addresses.isEmpty()) {
            final long val = addresses.getFirst().key();
            regression = regression.remove(0, val);
        }
        return this.createNewNode(Utils.tail(this.addresses), regression);
    }

    private IndexedNode head() {
        final int last = addresses.size() - 1;
        RegressionSearch<Long> regression = search;
        if (last >= 0) {
            long val = addresses.get(last).key();
            regression = regression.remove(last, val);
        }
        return this.createNewNode(Utils.head(this.addresses), regression);
    }

    private IndexedNode createNewNode(final List<Entry> keys, RegressionSearch<Long> regression) {
        final Node leaf = new LeafNode(this.table, keys, regression);
        return new LazyNode(() -> leaf, this.table.nodes().put(leaf));
    }

    private IndexedNode createNewNode(final List<Entry> keys) {
        final Node leaf = new LeafNode(this.table, keys);
        return new LazyNode(() -> leaf, this.table.nodes().put(leaf));
    }

    @Override
    public IndexedNode delete(final long key) {
        final int idx = this.search.search(this.keys(), key);
        if (idx < 0) {
            throw new IllegalArgumentException();
        }
        long val = addresses.get(idx).key();
        return this.createNewNode(
                Utils.withoutIdx(idx, this.addresses),
                search.remove(idx, val)
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
    public int getMinChildren() {
        return LEAF_MIN_CHILDREN;
    }

    @Override
    public int getMaxChildren() {
        return LEAF_MAX_CHILDREN;
    }

    @Override
    public Split insert(final long key, final String value) {
        int idx = this.search.search(this.keys(), key);

        final Entry entry = new OEntry(key, value, this.table.values().put(Map.entry(key, value)));

        final List<Entry> newKeys;
        RegressionSearch<Long> newModel = search;
        if (idx < 0) {
            idx = -idx - 1;
            newKeys = Utils.append(idx, entry, this.addresses);
            newModel = search.add(idx, key);
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
                        search.add(this.addresses.size(), stolenKey.key())),
                right.tail()
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
        final int idx = this.search.search(this.keys(), key);
        return idx < 0
                ? Optional.empty()
                : Optional.of(this.addresses.get(idx).value()).map(ValueLocation::value);
    }

    @Override
    public List<Map.Entry<Long, String>> rangeSearch(long minKey, long maxKey) {
        List<Map.Entry<Long, String>> result = new ArrayList<>();
        for (Entry entry : addresses) {
            long key = entry.key();
            if (key >= minKey && key <= maxKey) {
                result.add(Map.entry(key, entry.value().value()));
            }
        }
        return result;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void forEach(final Consumer<Entry> consumer) {
        this.addresses.forEach(consumer);
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