package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public final class ListOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public ListOperation(final Tree<Long, String> tree, final PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(final List<String> args) {
        final long offset = args.size() > 0 ? Long.parseLong(args.get(0)) : 0;
        final long count = args.size() > 1 ? Long.parseLong(args.get(1)) : Long.MAX_VALUE;
        this.stream.printf(this.tree.stream()
                .skip(offset)
                .limit(count)
                .map(e -> String.format(
                        "%s=%s",
                        e.getKey(), e.getValue())
                )
                .collect(Collectors.joining("\n"))
        );
    }

    @Override
    public String name() {
        return "list";
    }
}
