package sunmisc.btree.api;

import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

public interface Node extends Iterable<Entry> {

    Optional<Entry> firstEntry();

    Optional<Entry> lastEntry();

    Optional<String> search(long key);

    Split insert(long key, String value);

    IndexedNode delete(long key);

    IndexedNode delete(long key, String value);

    IndexedNode merge(Node other);
    
    SequencedMap<Long, String> rangeSearch(long minKey, long maxKey);

    int size();

    List<Long> keys();

    int minChildren();

    int maxChildren();

    @Deprecated
    IndexedNode withoutFirst();

    List<IndexedNode> stealFirstKeyFrom(Node right);

    List<IndexedNode> giveLastKeyTo(Node right);

    List<IndexedNode> children();


    default boolean isLeaf() {
        return this.children().isEmpty();
    }

    default boolean satisfiesMinChildren() {
        return this.size() >= this.minChildren();
    }
    default boolean satisfiesMaxChildren() {
        return this.size() <= this.maxChildren();
    }
    default boolean shouldSplit() {
        return !this.satisfiesMaxChildren();
    }
}
