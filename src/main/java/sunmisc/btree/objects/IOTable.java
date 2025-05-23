package sunmisc.btree.objects;

import sunmisc.btree.api.Node;
import sunmisc.btree.api.Objects;
import sunmisc.btree.api.Table;
import sunmisc.btree.api.Version;

import java.io.File;
import java.util.Map;

public final class IOTable implements Table {
    private final Objects<Map.Entry<Long, String>> values;
    private final Objects<Node> nodes;
    private final Objects<Version> roots;

    public IOTable(final File file) {
        this.values = new CachedObjects<>(new Values(file));
        this.nodes = new CachedObjects<>(new Nodes(file, this));
        this.roots = new CachedObjects<>(new Versions(file, this));
    }

    public IOTable(final String name) {
        this(makeFile(name));
    }

    private static File makeFile(final String name) {
        final File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    @Override
    public Objects<Node> nodes() {
        return this.nodes;
    }

    @Override
    public Objects<Map.Entry<Long, String>> values() {
        return this.values;
    }

    @Override
    public Objects<Version> roots() {
        return this.roots;
    }

    @Override
    public void delete() {
        this.values.delete();
        this.nodes.delete();
        this.roots.delete();
    }
}
