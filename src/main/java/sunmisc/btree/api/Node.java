package sunmisc.btree.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface Node {

    Optional<Entry> firstEntry();

    Optional<Entry> lastEntry();

    Optional<String> search(long key);

    Split insert(long key, String value);

    IndexedNode delete(long key);

    IndexedNode merge(Node other);

    List<IndexedNode> children();

    List<Long> keys();

    List<IndexedNode> stealFirstKeyFrom(Node right);

    List<IndexedNode> giveLastKeyTo(Node right);

    int size();

    // todo:
    IndexedNode tail();

    // todo:
    void forEach(Consumer<Entry> consumer);

    int getMinChildren();

    int getMaxChildren();

    default boolean isLeaf() {
        return size() == 0;
    }

    default boolean satisfiesMinChildren() {
        return size() >= getMinChildren();
    }

    default boolean satisfiesMaxChildren() {
        return size() <= getMaxChildren();
    }
    default boolean shouldSplit() {
        return !satisfiesMaxChildren();
    }
}
