package sunmisc.btree.api;

import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

public interface Node extends Iterable<Entry>{
    Optional<Entry> firstEntry();

    Optional<Entry> lastEntry();

    Optional<String> search(long key);

    Split insert(long key, String value);

    IndexedNode delete(long key);

    IndexedNode delete(long key, String value);

    IndexedNode merge(Node other);

    List<IndexedNode> children();

    List<Long> keys();

    List<IndexedNode> stealFirstKeyFrom(Node right);

    List<IndexedNode> giveLastKeyTo(Node right);

    SequencedMap<Long, String> rangeSearch(long minKey, long maxKey);

    int size();

    @Deprecated
    IndexedNode withoutFirst();

    int minChildren();
    int maxChildren();

    default boolean isLeaf() {
        return this.children().isEmpty();
    }

    default boolean satisfiesMinChildren() { return this.size() >= this.minChildren(); } // Проверяет минимальную заполненность
    default boolean satisfiesMaxChildren() { return this.size() <= this.maxChildren(); } // Проверяет максимальную заполненность
    default boolean shouldSplit() { return !this.satisfiesMaxChildren(); } //
}
