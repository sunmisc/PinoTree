package sunmisc.btree.api;

import java.io.IOException;
import java.util.List;

public interface Alloc extends Iterable<Long> {

    Page alloc() throws IOException;

    Page fetch(Location offset) throws IOException;

    default void free(final Location offset) throws IOException {
        this.free(List.of(offset));
    }

    void free(Iterable<Location> offsets) throws IOException;

    void clear() throws IOException;

    long last() throws IOException;
}
