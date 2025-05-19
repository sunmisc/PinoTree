package sunmisc.btree.api;

import java.util.Map;

public interface Table {

    Objects<Map.Entry<Long, String>> values();

    Objects<Node> nodes();

    Objects<Version> roots();

    void delete();
}
