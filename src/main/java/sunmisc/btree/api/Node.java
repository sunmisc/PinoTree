package sunmisc.btree.api;

import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

public interface Node extends Iterable<Entry>{
    Optional<Entry> firstEntry(); // Возвращает первую key-value пару узла
    Optional<Entry> lastEntry(); // Возвращает последнюю key-value пару узла
    Optional<String> search(long key); // Выполняет поиск значения по ключу
    Split insert(long key, String value); // Вставляет key-value пару, возвращая результат разделения
    IndexedNode delete(long key); // Удаляет ключ, возвращая обновленный узел
    IndexedNode merge(Node other); // Объединяет узел с другим
    List<IndexedNode> children(); // Возвращает список дочерних узлов
    List<Long> keys(); // Возвращает список ключей узла
    List<IndexedNode> stealFirstKeyFrom(Node right); // Перемещает первый ключ из правого узла
    List<IndexedNode> giveLastKeyTo(Node right); // Передает последний ключ правому узлу

    SequencedMap<Long, String> rangeSearch(long minKey, long maxKey);

    int size(); // Возвращает размер узла (число дочерних узлов или ключей)

    @Deprecated
    IndexedNode withoutFirst();

    int minChildren(); // Минимальное число дочерних узлов/ключей
    int maxChildren(); // Максимальное число дочерних узлов/ключей

    default boolean isLeaf() {
        return children().isEmpty();
    }

    default boolean satisfiesMinChildren() { return this.size() >= this.minChildren(); } // Проверяет минимальную заполненность
    default boolean satisfiesMaxChildren() { return this.size() <= this.maxChildren(); } // Проверяет максимальную заполненность
    default boolean shouldSplit() { return !this.satisfiesMaxChildren(); } //
}
