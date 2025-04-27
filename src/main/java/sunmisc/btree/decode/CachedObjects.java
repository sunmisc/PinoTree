package sunmisc.btree.decode;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;

import java.time.Duration;

public final class CachedObjects<T> implements Objects<T> {
    private final Cache<Long, T> cache;
    private final Objects<T> origin;

    public CachedObjects(Objects<T> origin) {
        this(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(15))
                .build(), origin);
    }
    public CachedObjects(Cache<Long, T> cache, Objects<T> origin) {
        this.cache = cache;
        this.origin = origin;
    }

    @Override
    public Location alloc(T value) {
        final Location loc = origin.alloc(value);
        cache.put(loc.offset(), value);
        return loc;
    }

    @Override
    public T fetch(long index) {
        return cache.get(index, origin::fetch);
    }

    @Override
    public void free(long index) {
        cache.invalidate(index);
        origin.free(index);
    }

    @Override
    public void free(Iterable<Long> indexes) {
        cache.invalidateAll(indexes);
        origin.free(indexes);
    }

    @Override
    public void delete() {
        cache.cleanUp();
    }
}
