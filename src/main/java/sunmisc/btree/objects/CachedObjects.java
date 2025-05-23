package sunmisc.btree.objects;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class CachedObjects<T> implements Objects<T> {
    private final Cache<Long, T> cache;
    private final Objects<T> origin;

    public CachedObjects(final Objects<T> origin) {
        this(Caffeine.newBuilder()
                .maximumSize(1024)
                .expireAfterAccess(Duration.ofMinutes(15))
                .build(), origin);
    }
    public CachedObjects(final Cache<Long, T> cache, final Objects<T> origin) {
        this.cache = cache;
        this.origin = origin;
    }

    @Override
    public Location put(final T value) {
        final Location loc = this.origin.put(value);
        this.cache.put(loc.offset(), value);
        return loc;
    }

    @Override
    public T fetch(final Location index) {
        return this.cache.get(index.offset(), _ -> origin.fetch(index));
    }

    @Override
    public void free(final Location index) {
        this.cache.invalidate(index.offset());
        this.origin.free(index);
    }

    @Override
    public void free(final Iterable<Location> indexes) {
        this.cache.invalidateAll(
                StreamSupport.stream(indexes.spliterator(), false)
                        .map(Location::offset)
                        .toList()
        );
        this.origin.free(indexes);
    }

    @Override
    public Optional<Location> lastIndex() {
        return origin.lastIndex();
    }

    @Override
    public void delete() {
        this.cache.cleanUp();
    }

    @Override
    public Iterator<Location> iterator() {
        return origin.iterator();
    }
}
