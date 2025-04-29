package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.objects.Table;
import sunmisc.btree.objects.TimeVersion;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class MutBtree implements Tree<Long, String> {
    private final Lock lock = new ReentrantLock();
    private final AtomicReference<Location> root;
    private final Table table;

    public MutBtree() {
        this(new Table("test"));
    }

    public MutBtree(final Table table) {
        this(table, null);
    }

    public MutBtree(final Table table, Location root) {
        this.table = table;
        this.root = new AtomicReference<>(root);
    }

    @Override
    public void put(final Long key, final String value) {
        this.lock.lock();
        try {
            final Location loc = this.root.getPlain();
            final Split split = (loc == null
                    ? new LeafNode(this.table, List.of())
                    : this.table.nodes().fetch(loc.offset())
            ).insert(key, value);
            final IndexedNode newRoot;
            if (split.rebalanced()) {
                final Node node = new InternalNode(this.table,
                        List.of(split.medianKey()),
                        List.of(split.src(), split.right())
                );
                newRoot = new LazyNode(() -> node, this.table.nodes().put(node));
            } else {
                newRoot = split.src();
            }
            final Version version = new TimeVersion(newRoot.offset());
            this.table.roots().put(version);
            this.root.setRelease(version);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Optional<String> get(final Long key) {
        final Location loc = this.root.getAcquire();
        if (loc == null) {
            return Optional.empty();
        }
        final Node prev = this.table.nodes().fetch(loc.offset());
        return prev.search(key);
    }

    @Override
    public void delete(final Long key) {
        this.lock.lock();
        try {
            final Location loc = this.root.getPlain();
            final Node prev = this.table.nodes().fetch(loc.offset());
            IndexedNode deleted = prev.delete(key);
            if (deleted.size() == 1 && !deleted.isLeaf()) {
                deleted = deleted.children().getFirst();
            }
            final Version version = new TimeVersion(deleted.offset());
            this.table.roots().put(version);
            this.root.setRelease(version);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Optional<Map.Entry<Long, String>> first() {
        final Location loc = this.root.getAcquire();
        return loc == null
                ? Optional.empty()
                : this.table.nodes()
                .fetch(loc.offset())
                .firstEntry()
                .map(raw -> Map.entry(raw.key(), raw.value().value()));
    }

    @Override
    public Optional<Map.Entry<Long, String>> last() {
        final Location loc = this.root.getAcquire();
        return loc == null
                ? Optional.empty()
                : this.table.nodes()
                .fetch(loc.offset())
                .lastEntry()
                .map(raw -> Map.entry(raw.key(), raw.value().value()));
    }

    @Override
    public Iterator<Map.Entry<Long, String>> iterator() {
        // todo:
        final Location loc = this.root.getAcquire();
        if (loc == null) {
            return Collections.emptyIterator();
        }
        final List<Map.Entry<Long, String>> res = new ArrayList<>();
        final Node prev = this.table.nodes().fetch(loc.offset());
        prev.forEach(e -> res.add(Map.entry(e.key(), e.value().value())));
        return res.iterator();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        forEach(e -> joiner.add(e.toString()));
        return joiner.toString();
    }
}
