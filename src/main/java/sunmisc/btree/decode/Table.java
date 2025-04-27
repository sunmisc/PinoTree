package sunmisc.btree.decode;

import sunmisc.btree.api.IndexedNode;
import sunmisc.btree.api.Node;
import sunmisc.btree.api.Objects;

import java.io.File;
import java.util.Map;

public final class Table {
    private final Objects<Map.Entry<Long, String>> values;
    private final Objects<Node> nodes;
    private final Objects<IndexedNode> roots;

    public Table(final File file) {
        this.values = new CachedObjects<>(new Values(file));
        this.nodes = new CachedObjects<>(new Nodes(file, this));
        this.roots = new CachedObjects<>(new Versions(file, this));
    }

    public Table(final String name) {
        final File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.values = new CachedObjects<>(new Values(file));
        this.nodes = new CachedObjects<>(new Nodes(file, this));
        this.roots = new CachedObjects<>(new Versions(file, this));
    }

    public Objects<Node> nodes() {
        return this.nodes;
    }

    public Objects<Map.Entry<Long, String>> values() {
        return this.values;
    }

    public Objects<IndexedNode> roots() {
        return this.roots;
    }

    public void delete() {
        this.values.delete();
        this.nodes.delete();
    }
}
