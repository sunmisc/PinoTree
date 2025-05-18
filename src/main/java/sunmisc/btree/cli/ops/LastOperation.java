package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintStream;
import java.util.List;

public final class LastOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintStream stream;

    public LastOperation(Tree<Long, String> tree, PrintStream stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        stream.println(tree.last()
                .map(e -> String.format(
                        "last: %d -> %s",
                        e.getKey(),
                        e.getValue())
                ).orElse("tree is empty"));
    }

    @Override
    public String name() {
        return "last";
    }
}