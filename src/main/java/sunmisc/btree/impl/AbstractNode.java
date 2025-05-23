package sunmisc.btree.impl;

import sunmisc.btree.api.IndexedNode;
import sunmisc.btree.api.Node;
import sunmisc.btree.api.Table;

import java.util.Collections;
import java.util.List;

public abstract class AbstractNode implements Node {
    public static final int ORDER = 128; // 64

    public static final int LEAF_MIN_CHILDREN = Math.ceilDiv(ORDER, 2) - 1;
    public static final int LEAF_MAX_CHILDREN = ORDER - 1;
    public static final int INTERNAL_MIN_CHILDREN = Math.ceilDiv(ORDER, 2);
    public static final int INTERNAL_MAX_CHILDREN = ORDER;

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