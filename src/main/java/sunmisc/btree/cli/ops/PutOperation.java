package sunmisc.btree.cli.ops;

import sunmisc.btree.api.Tree;

import java.util.List;

public final class PutOperation implements Operation {
    private final Tree<Long, String> tree;

    public PutOperation(Tree<Long, String> tree) {
        this.tree = tree;
    }

    @Override
    public void apply(List<String> args) {
        final int n = args.size();
        if ((n & 1) != 0) {
            throw new InternalError("length is odd");
        }
        for (int i = 0; i < n; i += 2) {
            long key = Long.parseLong(args.get(i));
            String value = args.get(i + 1);
            tree.put(key, value);
        }
    }
    @Override
    public String name() {
        return "put";
    }
}
