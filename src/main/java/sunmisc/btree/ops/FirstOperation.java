package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;

public final class FirstOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public FirstOperation(Tree<Long, String> tree, PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        stream.println(tree.first()
                .map(e -> String.format(
                        "%d = %s",
                        e.getKey(),
                        e.getValue())
                ).orElse("tree is empty"));
    }

    @Override
    public String name() {
        return "first";
    }
}
