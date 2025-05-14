package sunmisc.btree.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface Node {
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

    List<Map.Entry<Long, String>> rangeSearch(long minKey, long maxKey);

    int size(); // Возвращает размер узла (число дочерних узлов или ключей)
    IndexedNode tail(); // Возвращает хвостовой узел (заглушка для будущей реализации)
    void forEach(Consumer<Entry> consumer); // Итерирует по key-value парам
    int getMinChildren(); // Минимальное число дочерних узлов/ключей
    int getMaxChildren(); // Максимальное число дочерних узлов/ключей
    default boolean isLeaf() { return this.size() == 0; } // Проверяет, является ли узел листовым
    default boolean satisfiesMinChildren() { return this.size() >= this.getMinChildren(); } // Проверяет минимальную заполненность
    default boolean satisfiesMaxChildren() { return this.size() <= this.getMaxChildren(); } // Проверяет максимальную заполненность
    default boolean shouldSplit() { return !this.satisfiesMaxChildren(); } //
}
