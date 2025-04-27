package sunmisc.btree.decode;

import sunmisc.btree.alloc.CowAlloc;
import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.Alloc;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;
import sunmisc.btree.impl.IndexedNode;
import sunmisc.btree.impl.LazyNode;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public final class Versions implements Objects<IndexedNode> {
    private static final int PAGE_SIZE = 8;
    private final Alloc alloc;
    private final Table table;

    public Versions(Alloc alloc, Table table) {
        this.alloc = alloc;
        this.table = table;
    }

    public Versions(File parent, Table table) {
        this(new CowAlloc(
                new File(parent, "versions"), PAGE_SIZE),
                table
        );
    }

    @Override
    public Location alloc(IndexedNode node) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(bytes);
            data.writeLong(node.offset());
            return new LongLocation(
                    alloc.allocOne(new DataInputStream(
                            new ByteArrayInputStream(bytes.toByteArray())))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IndexedNode fetch(long index) {
        try {
            DataInputStream input = new DataInputStream(alloc.fetch(index));
            long off = input.readLong();
            return new LazyNode(table, new LongLocation(off));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(Iterable<Long> indexes) {
        try {
            for (Long index : indexes) {
                DataInputStream input = new DataInputStream(alloc.fetch(index));
                long off = input.readLong();
                IndexedNode node = new LazyNode(table, new LongLocation(off));
                List<Long> addressesNode = new LinkedList<>();
                List<Long> addressesValues = new LinkedList<>();
                recursiveFree(node, addressesNode, addressesValues);

                table.nodes().free(addressesNode);
                table.values().free(addressesValues);
                alloc.free(index);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void recursiveFree(IndexedNode node,
                               List<Long> addressesNode,
                               List<Long> addressesValues) {
        addressesNode.add(node.offset());
        node.children().forEach(e -> {
            recursiveFree(e, addressesNode, addressesValues);
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
            alloc.clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
