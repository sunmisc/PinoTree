package sunmisc.btree.decode;


import sunmisc.btree.api.Node;
import sunmisc.btree.api.Objects;
import sunmisc.btree.impl.IndexedNode;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class Table {
    private final Objects<Map.Entry<Long, String>> values;
    private final Objects<Node> nodes;
    private final Objects<IndexedNode> roots;

    public Table(String name) throws IOException {
        File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.values = new CachedObjects<>(new Values(file));
        this.nodes = new CachedObjects<>(new Nodes(file, this));
        this.roots = new CachedObjects<>(new Versions(file, this));
    }

    public Objects<Node> nodes() {
        return nodes;
    }

    public Objects<Map.Entry<Long, String>> values() {
        return values;
    }

    public Objects<IndexedNode> roots() {
        return roots;
    }

    public void delete() {
        values.delete();
        nodes.delete();
    }
}
