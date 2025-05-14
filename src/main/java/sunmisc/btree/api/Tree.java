package sunmisc.btree.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Tree<K,V> extends Iterable<Map.Entry<K, V>> {

    void put(K key, V value);

    Optional<String> get(K key);

    void delete(K key);

    Optional<Map.Entry<K, V>> first();

    Optional<Map.Entry<K, V>> last();

    Node root();

    List<Map.Entry<Long, String>> rangeSearch(long minKey, long maxKey);
}
