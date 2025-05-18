package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintStream;
import java.util.List;

public final class GetOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintStream stream;

    public GetOperation(Tree<Long, String> tree, PrintStream stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        for (String arg : args) {
            long key = Long.parseLong(arg);
            tree.get(key).ifPresentOrElse(val -> {
                this.stream.printf("Key %d: %s%n", key, val);
            }, () -> {
                this.stream.printf("Key %d not found%n", key);
            });
        }
    }

    @Override
    public String name() {
        return "get";
    }
}
