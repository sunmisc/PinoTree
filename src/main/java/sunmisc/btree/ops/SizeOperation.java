package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;

public final class SizeOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public SizeOperation(Tree<Long, String> tree, PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        stream.println(tree.size());
    }

    @Override
    public String name() {
        return "size";
    }
}
