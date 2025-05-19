package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public final class DeleteOperation implements Operation {
    private final Tree<Long, String> tree;

    public DeleteOperation(Tree<Long, String> tree) {
        this.tree = tree;
    }

    @Override
    public void apply(List<String> args) {
        args.stream().mapToLong(Long::parseLong).forEach(tree::delete);
    }

    @Override
    public String name() {
        return "delete";
    }
}
