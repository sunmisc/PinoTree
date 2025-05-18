package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.api.Objects;
import sunmisc.btree.objects.Table;
import sunmisc.btree.objects.TimeVersion;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.StreamSupport;

public final class MutBtree implements Tree<Long, String> {
    private static final AtomicInteger ID = new AtomicInteger();
    private final Lock lock = new ReentrantLock();
    private final Table table;

    public MutBtree() {
        this(new Table(String.format("test-%s", ID.getAndIncrement())));
    }

    public MutBtree(final Table table) {
        this.table = table;
    }

    private Optional<Node> fetchRoot() {
        return table.roots()
                .lastObject()
                .map(e -> this.table.nodes().fetch(e));
    }
    @Override 
    public void put(final Long key, final String value) {
        lock.lock();
        try {
            Objects<Version> versions = table.roots();
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
                newRoot = new LazyNode(() -> node, this.table.nodes().put(node));
            } else {
                newRoot = split.src();
            }
            final Version version = new TimeVersion(newRoot.offset());
            versions.put(version);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<String> get(final Long key) {
        return fetchRoot().flatMap(x -> x.search(key));
    }

    @Override
    public void delete(final Long key) {
        lock.lock();
        try {
            Objects<Version> versions = table.roots();
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
            lock.unlock();
        }
    }

    @Override
    public Optional<Map.Entry<Long, String>> first() {
        return fetchRoot()
                .flatMap(Node::firstEntry)
                .map(raw -> Map.entry(raw.key(), raw.value().value()));
    }

    @Override
    public Optional<Map.Entry<Long, String>> last() {
        return fetchRoot()
                .flatMap(Node::lastEntry)
                .map(raw -> Map.entry(raw.key(), raw.value().value()));
    }

    @Override
    public SequencedMap<Long, String> rangeSearch(Long minKey, Long maxKey) {
        if (minKey > maxKey) {
            throw new IllegalArgumentException("minKey > maxKey");
        }
        return fetchRoot()
                .map(e -> e.rangeSearch(minKey, maxKey))
                .orElse(Collections.emptyNavigableMap());
    }

    @Override
    public Iterator<Map.Entry<Long, String>> iterator() {
        return fetchRoot()
                .map(prev -> StreamSupport.stream(prev.spliterator(), false)
                        .map(e -> Map.entry(e.key(), e.value().value()))
                        .iterator()
                ).orElse(Collections.emptyIterator());
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        forEach(e -> joiner.add(e.toString()));
        return joiner.toString();
    }

    @Override
    public Node root() {
        return fetchRoot().orElseThrow();
    }
    public static void main(String[] args) {
        MutBtree tree = new MutBtree(new Table("kek1"));

        tree.put(1L, "one");
        tree.put(2L, "two");
        tree.put(3L, "three");
        tree.put(5L, "five");


        System.out.println("3 "+tree.first());
        SequencedMap<Long, String> range = tree.rangeSearch(2L, 4L);
        System.out.println(range);
    }
}
