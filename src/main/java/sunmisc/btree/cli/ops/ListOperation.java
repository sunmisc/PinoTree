package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintStream;
import java.util.List;

public final class ListOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintStream stream;

    public ListOperation(Tree<Long, String> tree, PrintStream stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        tree.forEach(entry -> {
            stream.printf("%s -> %s%n", entry.getKey(), entry.getValue());
        });
    }

    @Override
    public String name() {
        return "list";
    }
}
