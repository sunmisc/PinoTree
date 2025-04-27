package sunmisc.btree.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public interface Alloc {

    long allocOne(InputStream stream) throws IOException;

    Stream<Long> alloc(InputStream stream) throws IOException;

    InputStream fetch(long offset) throws IOException;

    default void free(final long offset) throws IOException {
        this.free(List.of(offset));
    }

    void free(Iterable<Long> offsets) throws IOException;

    void clear() throws IOException;
}
