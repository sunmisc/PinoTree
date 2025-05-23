package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;

public final class SizeOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public SizeOperation(final Tree<Long, String> tree, final PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(final List<String> args) {
        this.stream.println(this.tree.size());
    }

    @Override
    public String name() {
        return "size";
    }
}
