package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintStream;
import java.util.List;

public final class FirstOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintStream stream;

    public FirstOperation(Tree<Long, String> tree, PrintStream stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        stream.println(tree.first()
                .map(e -> String.format(
                        "first: %d -> %s",
                        e.getKey(),
                        e.getValue())
                ).orElse("tree is empty"));
    }

    @Override
    public String name() {
        return "first";
    }
}
