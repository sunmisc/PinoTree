package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.api.Objects;
import sunmisc.btree.objects.HeapTable;
import sunmisc.btree.objects.TimeVersion;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.StreamSupport;

public final class MutBtree implements Tree<Long, String> {
    private final Lock lock = new ReentrantLock();
    private final Table table;

    public MutBtree() {
        this(new HeapTable());
    }

    public MutBtree(final Table table) {
        this.table = table;
    }

    private Optional<Node> fetchRoot() {
        return this.table.roots()
                .lastObject()
                .map(e -> this.table.nodes().fetch(e));
    }

    @Override 
    public void put(final Long key, final String value) {
        this.lock.lock();
        try {
            final Objects<Version> versions = this.table.roots();
            final Split split = versions.lastObject()
                    .map(e -> this.table.nodes().fetch(e))
                    .orElseGet(() -> new LeafNode(this.table, List.of()))
                    .insert(key, value);
            final IndexedNode newRoot;
            if (split.rebalanced()) {
                final Node node = new InternalNode(this.table,
                        List.of(split.medianKey()),
                        List.of(split.src(), split.right())
                );
                newRoot = new FwdNode(node, this.table.nodes().put(node));
            } else {
                newRoot = split.src();
            }
            final Version version = new TimeVersion(newRoot.offset());
            versions.put(version);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Optional<String> get(final Long key) {
        return this.fetchRoot().flatMap(x -> x.search(key));
    }

    @Override
    public void delete(final Long key) {
        this.lock.lock();
        try {
            final Objects<Version> versions = this.table.roots();
            versions.lastObject()
                    .map(e -> this.table.nodes().fetch(e))
                    .ifPresent(prev -> {
                        IndexedNode deleted = prev.delete(key);
                        if (deleted.size() == 1 && !deleted.isLeaf()) {
                            deleted = deleted.children().getFirst();
                        }
                        final Version version = new TimeVersion(deleted.offset());
                        versions.put(version);
                    });
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void delete(final Long key, final String value) {
        this.lock.lock();
        try {
            final Objects<Version> versions = this.table.roots();
            versions.lastObject()
                    .map(e -> this.table.nodes().fetch(e))
                    .ifPresent(prev -> {
                        IndexedNode deleted = prev.delete(key, value);
                        if (deleted.size() == 1 && !deleted.isLeaf()) {
                            deleted = deleted.children().getFirst();
                        }
                        final Version version = new TimeVersion(deleted.offset());
                        versions.put(version);
                    });
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Optional<Map.Entry<Long, String>> first() {
        return this.fetchRoot()
                .flatMap(Node::firstEntry)
                .map(raw -> Map.entry(raw.key(), raw.value().value()));
    }

    @Override
    public Optional<Map.Entry<Long, String>> last() {
        return this.fetchRoot()
                .flatMap(Node::lastEntry)
                .map(raw -> Map.entry(raw.key(), raw.value().value()));
    }

    @Override
    public SequencedMap<Long, String> rangeSearch(final Long minKey, final Long maxKey) {
        if (minKey > maxKey) {
            throw new IllegalArgumentException("minKey > maxKey");
        }
        return this.fetchRoot()
                .map(e -> e.rangeSearch(minKey, maxKey))
                .orElse(Collections.emptyNavigableMap());
    }

    @Override
    public int size() {
        return this.fetchRoot()
                .map(this::count)
                .orElse(0);
    }
    private int count(final Node node) {
        if (node.isLeaf()) {
            return node.size();
        }
        return node.children()
                .stream()
                .reduce(0,
                        (a, b) -> a + this.count(b),
                        Integer::sum
                );
    }

    @Override
    public Iterator<Map.Entry<Long, String>> iterator() {
        return this.fetchRoot()
                .map(prev -> StreamSupport.stream(prev.spliterator(), false)
                        .map(e -> Map.entry(e.key(), e.value().value()))
                        .iterator()
                ).orElse(Collections.emptyIterator());
    }

    @Override
    public String toString() {
        final StringJoiner joiner = new StringJoiner(", ", "[", "]");
        this.forEach(e -> joiner.add(e.toString()));
        return joiner.toString();
    }
}
