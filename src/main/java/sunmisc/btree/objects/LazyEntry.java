package sunmisc.btree.objects;

import sunmisc.btree.alloc.TextLocation;
import sunmisc.btree.api.Entry;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;
import sunmisc.btree.api.ValueLocation;
import sunmisc.btree.utils.ConcurrentLazy;

import java.util.Map;

public final class LazyEntry implements Entry {
    private final long key;
    private final ConcurrentLazy<ValueLocation> lazy;

    public LazyEntry(final long key,
                     final Location index,
                     final Objects<Map.Entry<Long, String>> values) {
        this.key = key;
        this.lazy = new ConcurrentLazy<>(() -> {
            final Map.Entry<Long, String> entry = values.fetch(index);
            return new TextLocation(entry.getValue(), index);
        });
    }

    @Override
    public long key() {
        return this.key;
    }

    @Override
    public ValueLocation value() {
        return this.lazy.get();
    }
}
