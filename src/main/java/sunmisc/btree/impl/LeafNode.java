package sunmisc.btree.impl;

import sunmisc.btree.LearnedModel;
import sunmisc.btree.api.Entry;
import sunmisc.btree.api.Node;
import sunmisc.btree.api.Split;
import sunmisc.btree.decode.OEntry;
import sunmisc.btree.decode.Table;

import java.util.*;
import java.util.function.Consumer;

import static sunmisc.btree.impl.Constants.LEAF_MAX_CHILDREN;
import static sunmisc.btree.impl.Constants.LEAF_MIN_CHILDREN;

public final class LeafNode extends AbstractNode {
    private final LearnedModel model;
    private final List<Entry> addresses;

    public LeafNode(Table table, List<Entry> keys) {
        super(table, new ArrayList<>());
        this.addresses = keys;
        this.model = LearnedModel.retrain(keys());
    }

    @Override
    public int size() {
        return keys().size();
    }

    @Override
    public IndexedNode tail() {
        return createNewNode(Utils.tail(addresses));
    }

    private IndexedNode head() {
        return createNewNode(Utils.head(addresses));
    }

    private IndexedNode createNewNode(List<Entry> keys) {
        final Node leaf = new LeafNode(table, keys);
        return new LazyNode(() -> leaf, table.nodes().alloc(leaf));
    }

    @Override
    public IndexedNode delete(long key) {
        int idx = model.search(keys(), key);
        if (idx < 0) {
            throw new IllegalArgumentException();
        }
        return createNewNode(Utils.withoutIdx(idx, addresses));
    }

    @Override
    public IndexedNode merge(Node other) {
        if (!other.isLeaf()) {
            throw new IllegalArgumentException("Can only merge with another Leaf node");
        }
        List<Entry> concat = new ArrayList<>(addresses);
        other.forEach(concat::add);
        return createNewNode(concat);
    }

    @Override
    public List<Long> keys() {
        return new KeyListWrapper(addresses);
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
    public Split insert(long key, String value) {
        int idx = model.search(keys(), key);

        Entry entry = new OEntry(key, value, table.values().alloc(Map.entry(key, value)));

        List<Entry> newKeys;
        if (idx < 0) {
            idx = -idx - 1;
            newKeys = Utils.append(idx, entry, addresses);
        } else {
            newKeys = Utils.set(idx, entry, addresses);
        }
        LeafNode newLeaf = new LeafNode(table, newKeys);
        return newLeaf.shouldSplit()
                ? newLeaf.split()
                : new Split.UnarySplit(
                        new LazyNode(
                                () -> newLeaf,
                                table.nodes().alloc(newLeaf)
                        )
        );
    }

    public Split split() {
        int cutoff = addresses.size() >>> 1;
        long mid = addresses.get(cutoff).key();

        List<List<Entry>> keyPair = Utils.splitAt(cutoff, addresses);
        List<Entry> thisKeys = keyPair.get(0);
        List<Entry> otherKeys = keyPair.get(1);

        IndexedNode other = createNewNode(otherKeys);
        IndexedNode thisSplit = createNewNode(thisKeys);
        return new Split.RebalancedSplit(mid, thisSplit, other);
    }

    @Override
    public Entry firstEntry() {
        return addresses.getFirst();
    }

    @Override
    public List<IndexedNode> stealFirstKeyFrom(Node right) {
        Entry stolenKey = right.firstEntry();

        List<Entry> newKeys = Utils.append(
                addresses.size(), stolenKey, addresses);

        return List.of(
                createNewNode(newKeys),
                right.tail()
        );
    }

    @Override
    public List<IndexedNode> giveLastKeyTo(Node right) {
        Entry keyToGive = addresses.getLast();

        List<Entry> rightKeys = new ArrayList<>(
                right.size() + 1);
        rightKeys.add(keyToGive);
        right.forEach(rightKeys::add);

        return List.of(head(), createNewNode(rightKeys));
    }

    @Override
    public String search(long key) {
        int idx = model.searchEq(keys(), key);
        if (idx < 0) {
            throw new NoSuchElementException(key + " not found");
        }
        Entry entry = addresses.get(idx);
        return entry.value().value();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void forEach(Consumer<Entry> consumer) {
        addresses.forEach(consumer::accept);
    }

    private static final class KeyListWrapper extends AbstractList<Long> {
        private final List<Entry> entries;

        public KeyListWrapper(List<Entry> entries) {
            this.entries = entries;
        }

        @Override
        public Long get(int index) {
            return entries.get(index).key();
        }

        @Override
        public int size() {
            return entries.size();
        }
    }
}