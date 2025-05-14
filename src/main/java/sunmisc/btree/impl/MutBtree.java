package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.objects.Table;
import sunmisc.btree.objects.TimeVersion;

import java.util.*;

public final class MutBtree implements Tree<Long, String> {
    private final Table table;

    public MutBtree() {
        this(new Table("test"));
    }

    public MutBtree(final Table table) {
        this.table = table;
    }

    private Optional<Node> fetchRoot() {
        return table.roots().last()
                .map(e -> table.roots().fetch(e))
                .map(e -> this.table.nodes().fetch(e));
    }
    @Override
    public void put(final Long key, final String value) {
        final Split split = fetchRoot()
                .orElseGet(() -> new LeafNode(this.table, List.of())
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
    }

    @Override
    public Optional<String> get(final Long key) {
        return fetchRoot().flatMap(x -> x.search(key));
    }

    @Override
    public void delete(final Long key) {
        fetchRoot().ifPresent(prev -> {
            IndexedNode deleted = prev.delete(key);
            if (deleted.size() == 1 && !deleted.isLeaf()) {
                deleted = deleted.children().getFirst();
            }
            final Version version = new TimeVersion(deleted.offset());
            this.table.roots().put(version);
        });
    }

    @Override
    public Optional<Map.Entry<Long, String>> first() {
        return fetchRoot()
                .flatMap(x -> x.firstEntry()
                .map(raw -> Map.entry(raw.key(), raw.value().value())));
    }

    @Override
    public Optional<Map.Entry<Long, String>> last() {
        return fetchRoot()
                .flatMap(x -> x.lastEntry()
                .map(raw -> Map.entry(raw.key(), raw.value().value())));
    }

    @Override
    public List<Map.Entry<Long, String>> rangeSearch(long minKey, long maxKey) {
        if (minKey > maxKey) {
            throw new IllegalArgumentException("minKey > maxKey");
        }
        return fetchRoot().map(e -> e.rangeSearch(minKey, maxKey)).orElse(List.of());
    }

    @Override
    public Iterator<Map.Entry<Long, String>> iterator() {
        // todo:
        return fetchRoot().map(prev -> {
            final List<Map.Entry<Long, String>> res = new ArrayList<>();
            prev.forEach(e -> res.add(Map.entry(e.key(), e.value().value())));
            return res.iterator();
        }).orElse(Collections.emptyIterator());
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

        List<Map.Entry<Long, String>> range = tree.rangeSearch(2L, 4L);
        System.out.println(range);
    }
}
