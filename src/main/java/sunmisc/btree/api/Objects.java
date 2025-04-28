package sunmisc.btree.api;

import java.util.List;

// todo:
public interface Objects<T> extends Iterable<Location> {

    Location put(T value);

    T fetch(long index);

    default void free(final long index) {
        this.free(List.of(index));
    }
    void free(Iterable<Long> indexes);

    Location last();

    void delete();
}
