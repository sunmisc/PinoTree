package sunmisc.btree.objects;

import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public final class HeapTable implements Table {
    private final Objects<Version> roots = new HeapObjects<>();
    private final Objects<Map.Entry<Long, String>> values = new HeapObjects<>();
    private final Objects<Node> nodes = new HeapObjects<>();

    @Override
    public void delete() {
        this.roots.delete();
        this.values.delete();
        this.nodes.delete();
    }

    @Override
    public Objects<Version> roots() {
        return this.roots;
    }

    @Override
    public Objects<Map.Entry<Long, String>> values() {
        return this.values;
    }

    @Override
    public Objects<Node> nodes() {
        return this.nodes;
    }

    private static final class HeapObjects<T> implements Objects<T> {
        private final ConcurrentMap<Long, T> map = new ConcurrentHashMap<>();
        private final AtomicLong ids = new AtomicLong(-1);

        @Override
        public Location put(final T value) {
            final Location location = new LongLocation(this.ids.incrementAndGet());
            this.map.put(location.offset(), value);
            return location;
        }

        @Override
        public T fetch(final Location index) {
            return this.map.get(index.offset());
        }

        @Override
        public void free(final Iterable<Location> indexes) {
            indexes.forEach(this.map::remove);
        }

        @Override
        public Optional<Location> lastIndex() {
            final long i = this.ids.get();
            return i < 0 ? Optional.empty() : Optional.of(new LongLocation(i));
        }

        @Override
        public void delete() {
            this.map.clear();
            this.ids.set(-1);
        }

        @Override
        public Iterator<Location> iterator() {
            return this.map.keySet()
                    .stream()
                    .map(e -> (Location)new LongLocation(e))
                    .iterator();
        }
    }
}
