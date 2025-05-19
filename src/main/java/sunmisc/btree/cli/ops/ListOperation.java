package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ListOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintStream stream;

    public ListOperation(Tree<Long, String> tree, PrintStream stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(List<String> args) {
        stream.printf(StreamSupport.stream(tree.spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]\n"))
        );
    }

    @Override
    public String name() {
        return "list";
    }
}
