package sunmisc.btree.impl;

import sunmisc.btree.api.Entry;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Node;
import sunmisc.btree.api.Split;
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
        Node node = new LeafNode(table, new ArrayList<>());
        this.root = new LazyNode(() -> node, table.nodes().alloc(node));
        indexRoot = table.roots().alloc(root);
    }

    public void insert(long key, String value) {
        Split split = root.insert(key, value);
        table.roots().free(indexRoot.offset());
        if (split.rebalanced()) {
            List<IndexedNode> newChildren = List.of(split.src(), split.right());
            Node node = new InternalNode(table, List.of(split.medianKey()), newChildren);
            root = new LazyNode(() -> node, table.nodes().alloc(node));
        } else {
            root = split.src();
        }
        indexRoot = table.roots().alloc(root);
    }

    public void delete(long key) {
        try {
            table.roots().free(indexRoot.offset());
            IndexedNode r = root.delete(key);
            if (r.size() < 2 && !r.isLeaf()) {
                r = r.children().getFirst();
            }
            root = r;
            indexRoot = table.roots().alloc(root);
        } catch (Exception e) {

        }
    }

    public String search(long key) {
        try {
            return root.search(key);
        } catch (Exception e) {
            return null;
        }
    }

    public void print() {
        printTree(root, 0);
    }

    public Entry firstEntry() {
        return root.firstEntry();
    }

    public void printTree(Node node, int level) {
        if (node.isLeaf()) {
            System.out.print("Level " + level + " (Leaf): Keys = " + node.keys());
            System.out.println(", Values = " + node.keys());
        } else {
            System.out.println("Level " + level + " (Internal): Keys = " + node.keys());
            for (IndexedNode child : node.children()) {
                printTree(child, level + 1);
            }
        }
    }
    public void delete() {
        table.delete();
    }
    public static void main(String[] args) throws IOException {
        BTree btree = new BTree();
        btree.insert(1, "1");
        btree.insert(2, "2");
        btree.insert(3, "3");
        btree.insert(4, "4");
        btree.insert(5, "5");
    }
}