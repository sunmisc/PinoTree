package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintStream;
import java.util.List;

public final class RangeOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintStream stream;

    public RangeOperation(Tree<Long, String> tree, PrintStream stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        final long from = Long.parseLong(args.get(0));
        final long to = Long.parseLong(args.get(1));
        stream.println(tree.rangeSearch(from, to));
    }

    @Override
    public String name() {
        return "range";
    }
}
