package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.util.List;

public final class DeleteOperation implements Operation {
    private final Tree<Long, String> tree;

    public DeleteOperation(final Tree<Long, String> tree) {
        this.tree = tree;
    }

    @Override
    public void apply(final List<String> args) {
        args.stream().mapToLong(Long::parseLong).forEach(this.tree::delete);
    }

    @Override
    public String name() {
        return "delete";
    }
}
