package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ListOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public ListOperation(Tree<Long, String> tree, PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        long offset = args.size() > 0 ? Long.parseLong(args.get(0)) : 0;
        long count = args.size() > 1 ? Long.parseLong(args.get(1)) : Long.MAX_VALUE;
        stream.printf(StreamSupport.stream(tree.spliterator(), false)
                .skip(offset)
                .limit(count)
                .map(e -> String.format(
                        "%d = %s",
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
