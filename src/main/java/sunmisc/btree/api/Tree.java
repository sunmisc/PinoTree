package sunmisc.btree.api;

import java.util.Map;
import java.util.Optional;

public interface Tree<K,V> extends Iterable<Map.Entry<K, V>> {

    void put(K key, V value);

    String get(K key);

    void delete(K key);

    Map.Entry<K, V> first();

    Map.Entry<K, V> last();
}
