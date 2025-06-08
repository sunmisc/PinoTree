package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;

public final class LastOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public LastOperation(final Tree<Long, String> tree, final PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(final List<String> args) {
        this.stream.println(this.tree.last()
                .map(e -> String.format(
                        "%s=%s",
                        e.getKey(),
                        e.getValue())
                ).orElse("tree is empty"));
    }

    @Override
    public String name() {
        return "last";
    }
}