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
        roots.delete();
        values.delete();
        nodes.delete();
    }

    @Override
    public Objects<Version> roots() {
        return roots;
    }

    @Override
    public Objects<Map.Entry<Long, String>> values() {
        return values;
    }

    @Override
    public Objects<Node> nodes() {
        return nodes;
    }

    private static final class HeapObjects<T> implements Objects<T> {
        private final ConcurrentMap<Long, T> map = new ConcurrentHashMap<>();
        private final AtomicLong ids = new AtomicLong(-1);

        @Override
        public Location put(T value) {
            Location location = new LongLocation(ids.incrementAndGet());
            map.put(location.offset(), value);
            return location;
        }

        @Override
        public T fetch(Location index) {
            return map.get(index.offset());
        }

        @Override
        public void free(Iterable<Location> indexes) {
            indexes.forEach(map::remove);
        }

        @Override
        public Optional<Location> lastIndex() {
            final long i = ids.get();
            return i < 0 ? Optional.empty() : Optional.of(new LongLocation(i));
        }

        @Override
        public void delete() {
            map.clear();
            ids.set(-1);
        }

        @Override
        public Iterator<Location> iterator() {
            return map.keySet()
                    .stream()
                    .map(e -> (Location)new LongLocation(e))
                    .iterator();
        }
    }
}
