package sunmisc.btree.api;

import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Tree<K,V> extends Iterable<Map.Entry<K, V>> {

    void put(K key, V value);

    Optional<String> get(K key);

    void delete(K key);

    void delete(K key, V value);

    Optional<Map.Entry<K, V>> first();

    Optional<Map.Entry<K, V>> last();

    SequencedMap<Long, String> rangeSearch(K from, K to);

    int size();

    default Stream<Map.Entry<K, V>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
