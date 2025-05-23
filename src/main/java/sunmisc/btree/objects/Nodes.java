package sunmisc.btree.objects;

import sunmisc.btree.alloc.Malloc;
import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.*;
import sunmisc.btree.api.Objects;
import sunmisc.btree.impl.InternalNode;
import sunmisc.btree.impl.FwdNode;
import sunmisc.btree.impl.LeafNode;

import java.io.*;
import java.util.*;
import java.util.stream.StreamSupport;

public final class Nodes implements Objects<Node> {
    private static final int PAGE_SIZE = 4096;
    private final Alloc alloc;
    private final Table table;

    public Nodes(final File parent, final Table table) {
        this(new Malloc(
                new File(parent, "nodes"), PAGE_SIZE),
                table
        );
    }

    public Nodes(final Alloc alloc, final Table table) {
        this.alloc = alloc;
        this.table = table;
    }

    @Override
    public Location put(final Node node) {
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream data = new DataOutputStream(bytes);

            data.writeInt(node.children().size());
            for (final Location ks : node.children()) {
                data.writeLong(ks.offset());
            }
            data.writeInt(node.keys().size());
            if (node.isLeaf()) {
                node.forEach(e -> {
                    try {
                        data.writeLong(e.key());
                        data.writeLong(e.value().offset());
                    } catch (final IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } else {
                for (final Long key : node.keys()) {
                    data.writeLong(key);
                }
            }
            final Page page = this.alloc.alloc();
            page.write(new ByteArrayInputStream(bytes.toByteArray()));
            return page;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Node fetch(final Location index) {
        try {
            final Page page = this.alloc.fetch(index);
            final DataInputStream input = new DataInputStream(page.read());
            final int childSize = input.readInt();
            if (childSize == 0) {
                final int keysCount = input.readInt();
                final List<Entry> keys = new ArrayList<>(keysCount);
                for (int i = 0; i < keysCount; i++) {
                    final long key = input.readLong();
                    final long loc = input.readLong();
                    keys.add(new LazyEntry(key, new LongLocation(loc), this.table.values()));
                }
                return new LeafNode(this.table, keys);
            } else {
                final List<IndexedNode> children = new ArrayList<>();
                for (int i = 0; i < childSize; i++) {
                    final Location off = new LongLocation(input.readLong());
                    children.add(new FwdNode(
                            () -> this.table.nodes().fetch(off),
                            off)
                    );
                }
                final int keysCount = input.readInt();
                final List<Long> keys = new ArrayList<>(keysCount);
                for (int i = 0; i < keysCount; i++) {
                    final long key = input.readLong();
                    keys.add(key);
                }
                return new InternalNode(this.table, keys, children);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(final Iterable<Location> indexes) {
        try {
            final List<Location> child = new LinkedList<>();
            final List<Location> values = new LinkedList<>();
            for (final Location index : indexes) {
                final IndexedNode node = new FwdNode(this.table, index);
                this.recursiveFree(node, child, values);
            }
            this.alloc.free(child);
            this.table.values().free(values);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Location> lastIndex() {
        try {
            return Optional.of(new LongLocation(this.alloc.last()));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete() {
        try {
            this.alloc.clear();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<Location> iterator() {
        return StreamSupport.stream(this.alloc.spliterator(), false)
                .map(e -> (Location) new LongLocation(e))
                .iterator();
    }

    private void recursiveFree(final IndexedNode node,
                               final List<Location> child,
                               final List<Location> values) {
        node.children().forEach(e -> this.recursiveFree(e, child, values));
        if (node.isLeaf()) {
            node.forEach(e -> values.add(e.value()));
        }
        child.add(node);
    }
}
