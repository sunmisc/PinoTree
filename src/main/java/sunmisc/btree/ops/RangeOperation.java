package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public final class RangeOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public RangeOperation(Tree<Long, String> tree, PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        final long from = Long.parseLong(args.get(0));
        final long to = Long.parseLong(args.get(1));
        stream.println(tree.rangeSearch(from, to)
                .entrySet()
                .stream()
                .map(e -> String.format(
                        "%s = %s",
                        e.getKey(), e.getValue())
                )
                .collect(Collectors.joining("\n")));
    }

    @Override
    public String name() {
        return "range";
    }
}
