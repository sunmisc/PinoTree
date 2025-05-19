package sunmisc.btree.impl;

import sunmisc.btree.api.IndexedNode;
import sunmisc.btree.api.Node;
import sunmisc.btree.api.Table;

import java.util.Collections;
import java.util.List;

public abstract class AbstractNode implements Node {
    protected final Table table;
    private final List<IndexedNode> children;

    public AbstractNode(final Table table, final List<IndexedNode> children) {
        this.table = table;
        this.children = children;
    }

    @Override
    public int size() {
        return this.children.size();
    }

    @Override
    public List<IndexedNode> children() {
        return Collections.unmodifiableList(this.children);
    }
}