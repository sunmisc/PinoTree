package sunmisc.btree.ops;

import sunmisc.btree.api.Tree;

import java.io.PrintWriter;
import java.util.List;
import java.util.StringJoiner;

public final class GetOperation implements Operation {
    private final Tree<Long, String> tree;
    private final PrintWriter stream;

    public GetOperation(final Tree<Long, String> tree, final PrintWriter stream) {
        this.tree = tree;
        this.stream = stream;
    }

    @Override
    public void apply(final List<String> args) {
        final StringJoiner joiner = new StringJoiner("\n");
        for (final String arg : args) {
            final long key = Long.parseLong(arg);
            this.tree.get(key).ifPresentOrElse(val -> {
                joiner.add(String.format("%s=%s", key, val));
            }, () -> {
                joiner.add(String.format("%s=empty", key));
            });
        }
        this.stream.println(joiner);
    }

    @Override
    public String name() {
        return "get";
    }
}
