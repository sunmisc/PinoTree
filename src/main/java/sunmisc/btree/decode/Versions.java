package sunmisc.btree.decode;

import sunmisc.btree.alloc.CowAlloc;
import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.Alloc;
import sunmisc.btree.api.IndexedNode;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;
import sunmisc.btree.impl.LazyNode;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public final class Versions implements Objects<IndexedNode> {
    private static final int PAGE_SIZE = 8;
    private final Alloc alloc;
    private final Table table;

    public Versions(final Alloc alloc, final Table table) {
        this.alloc = alloc;
        this.table = table;
    }

    public Versions(final File parent, final Table table) {
        this(new CowAlloc(
                new File(parent, "versions"), PAGE_SIZE),
                table
        );
    }

    @Override
    public Location alloc(final IndexedNode node) {
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream data = new DataOutputStream(bytes);
            data.writeLong(node.offset());
            return new LongLocation(
                    this.alloc.allocOne(new DataInputStream(
                            new ByteArrayInputStream(bytes.toByteArray())))
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IndexedNode fetch(final long index) {
        try {
            final DataInputStream input = new DataInputStream(this.alloc.fetch(index));
            final long off = input.readLong();
            return new LazyNode(this.table, new LongLocation(off));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(final Iterable<Long> indexes) {
        try {
            for (final Long index : indexes) {
                final DataInputStream input = new DataInputStream(this.alloc.fetch(index));
                final long off = input.readLong();
                final IndexedNode node = new LazyNode(this.table, new LongLocation(off));
                final List<Long> addressesNode = new LinkedList<>();
                final List<Long> addressesValues = new LinkedList<>();
                this.recursiveFree(node, addressesNode, addressesValues);

                this.table.nodes().free(addressesNode);
                this.table.values().free(addressesValues);
                this.alloc.free(index);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void recursiveFree(final IndexedNode node,
                               final List<Long> addressesNode,
                               final List<Long> addressesValues) {
        addressesNode.add(node.offset());
        node.children().forEach(e -> {
            this.recursiveFree(e, addressesNode, addressesValues);
        });
        if (node.isLeaf()) {
            node.forEach(e -> {
                addressesValues.add(e.value().offset());
            });
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
}
