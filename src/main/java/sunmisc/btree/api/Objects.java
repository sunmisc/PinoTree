package sunmisc.btree.api;

import java.util.List;
import java.util.Optional;

// todo:
public interface Objects<T> extends Iterable<Location> {

    Location put(T value);

    T fetch(Location index);

    default void free(final long index) {
        this.free(List.of(index));
    }
    void free(Iterable<Long> indexes);

    Optional<Location> last();

    void delete();
}
