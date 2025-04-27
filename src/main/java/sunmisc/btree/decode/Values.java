package sunmisc.btree.decode;

import sunmisc.btree.alloc.CowAlloc;
import sunmisc.btree.api.Alloc;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;

import java.io.*;
import java.util.Map;

public final class Values implements Objects<Map.Entry<Long, String>> {
    private static final int PAGE_SIZE = 512;
    private final Alloc alloc;

    public Values(final File parent) {
        this(new CowAlloc(
                new File(parent, "values"),
                PAGE_SIZE)
        );
    }

    public Values(final Alloc alloc) {
        this.alloc = alloc;
    }

    @Override
    public Location alloc(final Map.Entry<Long, String> value) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final DataOutputStream data = new DataOutputStream(out)) {
            data.writeLong(value.getKey());
            data.writeUTF(value.getValue());
            final long off = this.alloc.allocOne(new ByteArrayInputStream(out.toByteArray()));
            return () -> off;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map.Entry<Long, String> fetch(final long index) {
        try (final ByteArrayInputStream in = new ByteArrayInputStream(
                this.alloc.fetch(index).readAllBytes());
             final DataInputStream data = new DataInputStream(in)) {
            return Map.entry(data.readLong(), data.readUTF());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(final Iterable<Long> indexes) {
        try {
            this.alloc.free(indexes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete() {

    }
}
