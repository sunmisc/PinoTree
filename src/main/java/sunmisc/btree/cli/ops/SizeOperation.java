package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintStream;
import java.util.List;

public final class SizeOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintStream stream;

    public SizeOperation(Tree<Long, String> tree, PrintStream stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        stream.printf("table size: %s%n", tree.size());
    }

    @Override
    public String name() {
        return "size";
    }
}
