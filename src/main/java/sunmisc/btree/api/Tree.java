package sunmisc.btree.api;

import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;

public interface Tree<K,V> extends Iterable<Map.Entry<K, V>> {

    void put(K key, V value);

    Optional<String> get(K key);

    void delete(K key);

    Optional<Map.Entry<K, V>> first();

    Optional<Map.Entry<K, V>> last();

    @Deprecated
    Node root();

    SequencedMap<Long, String> rangeSearch(K from, K to);
}
