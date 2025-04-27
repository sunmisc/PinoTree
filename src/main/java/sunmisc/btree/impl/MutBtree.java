package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.decode.Table;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class MutBtree implements Tree<Long, String> {
    private final Lock lock = new ReentrantLock();
    private final AtomicReference<Location> root = new AtomicReference<>();
    private final Table table;

    public MutBtree() {
        this(new Table("test"));
    }

    public MutBtree(Table table) {
        this.table = table;
    }

    @Override
    public void put(Long key, String value) {
        lock.lock();
        try {
            Location loc = root.getPlain();
            Split split = (loc == null
                    ? new LeafNode(table, List.of())
                    : table.nodes().fetch(loc.offset())
            ).insert(key, value);
            IndexedNode newRoot;
            if (split.rebalanced()) {
                Node node = new InternalNode(table,
                        List.of(split.medianKey()),
                        List.of(split.src(), split.right())
                );
                newRoot = new LazyNode(() -> node, table.nodes().alloc(node));
            } else {
                newRoot = split.src();
            }
            root.set(table.roots().alloc(newRoot));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String get(Long key) {
        Location loc = root.getAcquire();
        if (loc == null) {
            throw new IllegalArgumentException("Tree is empty");
        }
        IndexedNode prev = table.roots().fetch(loc.offset());
        return prev.search(key);
    }

    @Override
    public void delete(Long key) {
        lock.lock();
        try {
            Location loc = root.getPlain();
            if (loc == null) {
                throw new IllegalArgumentException("Tree is empty");
            }
            IndexedNode prev = table.roots().fetch(loc.offset());
            IndexedNode deleted = prev.delete(key);
            if (deleted.size() < 2 && !deleted.isLeaf()) {
                deleted = deleted.children().getFirst();
            }
            root.setRelease(table.roots().alloc(deleted));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map.Entry<Long, String> first() {
        Location loc = root.getAcquire();
        if (loc == null) {
            throw new IllegalArgumentException("Tree is empty");
        }
        IndexedNode prev = table.roots().fetch(loc.offset());
        Entry raw = prev.firstEntry();
        return Map.entry(raw.key(), raw.value().value());
    }

    @Override
    public Map.Entry<Long, String> last() {
        // todo:
        return Map.entry(0L, "0");
    }

    @Override
    public Iterator<Map.Entry<Long, String>> iterator() {
        // todo:
        Location loc = root.getAcquire();
        if (loc == null) {
            return Collections.emptyIterator();
        }
        List<Map.Entry<Long, String>> res = new ArrayList<>();
        IndexedNode prev = table.roots().fetch(loc.offset());
        prev.forEach(e -> res.add(Map.entry(e.key(), e.value().value())));
        return res.iterator();
    }
}
