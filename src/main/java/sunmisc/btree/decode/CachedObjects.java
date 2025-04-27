package sunmisc.btree.decode;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;

import java.time.Duration;

public final class CachedObjects<T> implements Objects<T> {
    private final Cache<Long, T> cache;
    private final Objects<T> origin;

    public CachedObjects(final Objects<T> origin) {
        this(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(15))
                .build(), origin);
    }
    public CachedObjects(final Cache<Long, T> cache, final Objects<T> origin) {
        this.cache = cache;
        this.origin = origin;
    }

    @Override
    public Location alloc(final T value) {
        final Location loc = this.origin.alloc(value);
        this.cache.put(loc.offset(), value);
        return loc;
    }

    @Override
    public T fetch(final long index) {
        return this.cache.get(index, this.origin::fetch);
    }

    @Override
    public void free(final long index) {
        this.cache.invalidate(index);
        this.origin.free(index);
    }

    @Override
    public void free(final Iterable<Long> indexes) {
        this.cache.invalidateAll(indexes);
        this.origin.free(indexes);
    }

    @Override
    public void delete() {
        this.cache.cleanUp();
    }
}
