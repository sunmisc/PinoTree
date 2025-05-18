package sunmisc.btree.cli.ops;

import java.util.List;

public interface Operation {

    void apply(List<String> args);

    String name();
}
