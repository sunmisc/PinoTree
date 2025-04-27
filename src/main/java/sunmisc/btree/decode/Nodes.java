package sunmisc.btree.decode;

import sunmisc.btree.alloc.CowAlloc;
import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.*;
import sunmisc.btree.impl.IndexedNode;
import sunmisc.btree.impl.InternalNode;
import sunmisc.btree.impl.LazyNode;
import sunmisc.btree.impl.LeafNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class Nodes implements Objects<Node> {
    private static final int PAGE_SIZE = 1024;
    private final Alloc alloc;
    private final Table table;

    public Nodes(File parent, Table table) {
        this(new CowAlloc(
                new File(parent, "nodes"), PAGE_SIZE),
                table
        );
    }

    public Nodes(Alloc alloc, Table table) {
        this.alloc = alloc;
        this.table = table;
    }

    @Override
    public Location alloc(Node node) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(bytes);

            data.writeInt(node.children().size());
            for (Location ks : node.children()) {
                data.writeLong(ks.offset());
            }
            data.writeInt(node.keys().size());
            if (node.isLeaf()) {
                node.forEach(e -> {
                    try {
                        data.writeLong(e.key());
                        data.writeLong(e.value().offset());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } else {
                for (Long key : node.keys()) {
                    data.writeLong(key);
                }
            }
            return new LongLocation(alloc.allocOne(
                    new ByteArrayInputStream(bytes.toByteArray()))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Node fetch(long index) {
        try {
            DataInputStream input = new DataInputStream(alloc.fetch(index));
            final int childSize = input.readInt();
            if (childSize == 0) {
                final int keysCount = input.readInt();
                List<Entry> keys = new ArrayList<>(keysCount);
                for (int i = 0; i < keysCount; i++) {
                    long key = input.readLong();
                    long loc = input.readLong();
                    keys.add(new LazyEntry(key, loc, table.values()));
                }
                return new LeafNode(table, keys);
            } else {
                List<IndexedNode> children = new ArrayList<>();
                for (int i = 0; i < childSize; i++) {
                    long off = input.readLong();
                    children.add(new LazyNode(
                            () -> table.nodes().fetch(off),
                            new LongLocation(off))
                    );
                }
                final int keysCount = input.readInt();
                List<Long> keys = new ArrayList<>(keysCount);
                for (int i = 0; i < keysCount; i++) {
                    long key = input.readLong();
                    keys.add(key);
                }
                return new InternalNode(table, keys, children);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(Iterable<Long> indexes) {
        try {
            alloc.free(indexes);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
