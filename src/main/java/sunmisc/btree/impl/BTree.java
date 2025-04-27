package sunmisc.btree.impl;

import sunmisc.btree.api.*;
import sunmisc.btree.decode.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BTree {
    private IndexedNode root;
    private Location indexRoot;
    private final Table table;

    public BTree() throws IOException {
        this.table = new Table("kek");
        final Node node = new LeafNode(this.table, new ArrayList<>());
        this.root = new LazyNode(() -> node, this.table.nodes().alloc(node));
        this.indexRoot = this.table.roots().alloc(this.root);
    }

    public void insert(final long key, final String value) {
        final Split split = this.root.insert(key, value);
        this.table.roots().free(this.indexRoot.offset());
        if (split.rebalanced()) {
            final List<IndexedNode> newChildren = List.of(split.src(), split.right());
            final Node node = new InternalNode(this.table, List.of(split.medianKey()), newChildren);
            this.root = new LazyNode(() -> node, this.table.nodes().alloc(node));
        } else {
            this.root = split.src();
        }
        this.indexRoot = this.table.roots().alloc(this.root);
    }

    public void delete(final long key) {
        try {
            this.table.roots().free(this.indexRoot.offset());
            IndexedNode r = this.root.delete(key);
            if (r.size() < 2 && !r.isLeaf()) {
                r = r.children().getFirst();
            }
            this.root = r;
            this.indexRoot = this.table.roots().alloc(this.root);
        } catch (final Exception e) {

        }
    }

    public String search(final long key) {
        try {
            return this.root.search(key).orElseThrow();
        } catch (final Exception e) {
            return null;
        }
    }

    public void print() {
        this.printTree(this.root, 0);
    }

    public Entry firstEntry() {
        return this.root.firstEntry().orElseThrow();
    }

    public void printTree(final Node node, final int level) {
        if (node.isLeaf()) {
            System.out.print("Level " + level + " (Leaf): Keys = " + node.keys());
            System.out.println(", Values = " + node.keys());
        } else {
            System.out.println("Level " + level + " (Internal): Keys = " + node.keys());
            for (final IndexedNode child : node.children()) {
                this.printTree(child, level + 1);
            }
        }
    }
    public void delete() {
        this.table.delete();
    }
    public static void main(final String[] args) throws IOException {
        final BTree btree = new BTree();
        btree.insert(1, "1");
        btree.insert(2, "2");
        btree.insert(3, "3");
        btree.insert(4, "4");
        btree.insert(5, "5");
    }
}