package sunmisc.btree.ops;

import java.util.List;

public final class EmptyOperation implements Operation {

    @Override
    public void apply(List<String> args) {
        System.out.println("Empty operation");
    }

    @Override
    public String name() {
        return "?";
    }
}
