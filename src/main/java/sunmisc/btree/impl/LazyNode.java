package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.objects.IOTable;
import sunmisc.btree.utils.ConcurrentLazy;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.Supplier;

public final class LazyNode implements IndexedNode {
    private final ConcurrentLazy<Node> lazy;
    private final Location offset;

    public LazyNode(final Table table, final Location offset) {
        this(new ConcurrentLazy<>(() -> table.nodes().fetch(offset)), offset);
    }

    public LazyNode(final Supplier<Node> supplier, final Location offset) {
        this(new ConcurrentLazy<>(supplier), offset);
    }

    public LazyNode(final ConcurrentLazy<Node> lazy, final Location offset) {
        this.lazy = lazy;
        this.offset = offset;
    }

    @Override
    public long offset() {
        return this.offset.offset();
    }

    @Override
    public Optional<Entry> firstEntry() {
        return this.lazy.get().firstEntry();
    }

    @Override
    public Optional<Entry> lastEntry() {
        return this.lazy.get().lastEntry();
    }

    @Override
    public Optional<String> search(final long key) {
        return this.lazy.get().search(key);
    }

    @Override
    public Split insert(final long key, final String value) {
        return this.lazy.get().insert(key, value);
    }

    @Override
    public IndexedNode delete(final long key) {
        return this.lazy.get().delete(key);
    }

    @Override
    public IndexedNode delete(long key, String value) {
        return this.lazy.get().delete(key, value);
    }

    @Override
    public IndexedNode merge(final Node other) {
        return this.lazy.get().merge(other);
    }

    @Override
    public List<IndexedNode> children() {
        return this.lazy.get().children();
    }

    @Override
    public List<Long> keys() {
        return this.lazy.get().keys();
    }

    @Override
    public List<IndexedNode> stealFirstKeyFrom(final Node right) {
        return this.lazy.get().stealFirstKeyFrom(right);
    }

    @Override
    public List<IndexedNode> giveLastKeyTo(final Node right) {
        return this.lazy.get().giveLastKeyTo(right);
    }

    @Override
    public int size() {
        return this.lazy.get().size();
    }

    @Override
    public IndexedNode withoutFirst() {
        return this.lazy.get().withoutFirst();
    }

    @Override
    public SequencedMap<Long, String> rangeSearch(long minKey, long maxKey) {
        return lazy.get().rangeSearch(minKey, maxKey);
    }

    @Override
    public int minChildren() {
        return this.lazy.get().minChildren();
    }

    @Override
    public int maxChildren() {
        return this.lazy.get().maxChildren();
    }

    @Override
    public boolean isLeaf() {
        return this.lazy.get().isLeaf();
    }

    @Override
    public boolean satisfiesMinChildren() {
        return this.lazy.get().satisfiesMinChildren();
    }

    @Override
    public boolean satisfiesMaxChildren() {
        return this.lazy.get().satisfiesMaxChildren();
    }

    @Override
    public boolean shouldSplit() {
        return this.lazy.get().shouldSplit();
    }

    @Override
    public Iterator<Entry> iterator() {
        return this.lazy.get().iterator();
    }
}
