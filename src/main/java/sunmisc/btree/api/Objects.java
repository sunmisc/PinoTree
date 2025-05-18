package sunmisc.btree.api;

import java.util.List;
import java.util.Optional;

// todo:
public interface Objects<T> extends Iterable<Location> {

    Location put(T value);

    T fetch(Location index);

    default void free(final Location index) {
        this.free(List.of(index));
    }
    void free(Iterable<Location> indexes);

    Optional<Location> lastIndex();

    default Optional<T> lastObject() {
        return this.lastIndex().map(this::fetch);
    }

    void delete();
}
