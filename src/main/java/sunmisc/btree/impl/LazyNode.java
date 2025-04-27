package sunmisc.btree.impl;

import sunmisc.btree.api.Entry;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Node;
import sunmisc.btree.api.Split;
import sunmisc.btree.decode.Table;
import sunmisc.btree.utils.ConcurrentLazy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LazyNode implements IndexedNode {
    private final ConcurrentLazy<Node> lazy;
    private final Location offset;

    public LazyNode(Table table, Location offset) {
        this(new ConcurrentLazy<>(() -> table.nodes().fetch(offset.offset())), offset);
    }

    public LazyNode(Supplier<Node> supplier, Location offset) {
        this(new ConcurrentLazy<>(supplier), offset);
    }

    public LazyNode(ConcurrentLazy<Node> lazy, Location offset) {
        this.lazy = lazy;
        this.offset = offset;
    }

    @Override
    public long offset() {
        return offset.offset();
    }

    @Override
    public Entry firstEntry() {
        return lazy.get().firstEntry();
    }

    @Override
    public String search(long key) {
        return lazy.get().search(key);
    }

    @Override
    public Split insert(long key, String value) {
        return lazy.get().insert(key, value);
    }

    @Override
    public IndexedNode delete(long key) {
        return lazy.get().delete(key);
    }

    @Override
    public IndexedNode merge(Node other) {
        return lazy.get().merge(other);
    }

    @Override
    public List<IndexedNode> children() {
        return lazy.get().children();
    }

    @Override
    public List<Long> keys() {
        return lazy.get().keys();
    }

    @Override
    public List<IndexedNode> stealFirstKeyFrom(Node right) {
        return lazy.get().stealFirstKeyFrom(right);
    }

    @Override
    public List<IndexedNode> giveLastKeyTo(Node right) {
        return lazy.get().giveLastKeyTo(right);
    }

    @Override
    public int size() {
        return lazy.get().size();
    }

    @Override
    public IndexedNode tail() {
        return lazy.get().tail();
    }

    @Override
    public void forEach(Consumer<Entry> consumer) {
        lazy.get().forEach(consumer);
    }

    @Override
    public int getMinChildren() {
        return lazy.get().getMinChildren();
    }

    @Override
    public int getMaxChildren() {
        return lazy.get().getMaxChildren();
    }

    @Override
    public boolean isLeaf() {
        return lazy.get().isLeaf();
    }

    @Override
    public boolean satisfiesMinChildren() {
        return lazy.get().satisfiesMinChildren();
    }

    @Override
    public boolean satisfiesMaxChildren() {
        return lazy.get().satisfiesMaxChildren();
    }

    @Override
    public boolean shouldSplit() {
        return lazy.get().shouldSplit();
    }
}
