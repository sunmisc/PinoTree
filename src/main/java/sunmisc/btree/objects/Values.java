package sunmisc.btree.objects;

import sunmisc.btree.alloc.Malloc;
import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.Alloc;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;
import sunmisc.btree.api.Page;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class Values implements Objects<Map.Entry<Long, String>> {
    private static final int PAGE_SIZE = 512;
    private final Alloc alloc;

    public Values(final File parent) {
        this(new Malloc(
                new File(parent, "values"),
                PAGE_SIZE)
        );
    }

    public Values(final Alloc alloc) {
        this.alloc = alloc;
    }

    @Override
    public Location put(final Map.Entry<Long, String> value) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final DataOutputStream data = new DataOutputStream(out)) {
            data.writeLong(value.getKey());
            data.writeUTF(value.getValue());
            final Page page = this.alloc.alloc();
            page.write(new ByteArrayInputStream(out.toByteArray()));
            return page;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map.Entry<Long, String> fetch(final Location index) {
        try (final InputStream in = this.alloc.fetch(index).read();
             final DataInputStream data = new DataInputStream(in)) {
            return Map.entry(data.readLong(), data.readUTF());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(final Iterable<Location> indexes) {
        try {
            this.alloc.free(indexes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete() {

    }


    @Override
    public Optional<Location> lastIndex() {
        try {
            return Optional.of(new LongLocation(this.alloc.last()));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public Iterator<Location> iterator() {
        return StreamSupport.stream(this.alloc.spliterator(), false)
                .map(e -> (Location) new LongLocation(e))
                .iterator();
    }
}
