package sunmisc.btree.decode;

import sunmisc.btree.alloc.CowAlloc;
import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.Alloc;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;
import sunmisc.btree.api.Version;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;

public final class Versions implements Objects<Version> {
    private static final int PAGE_SIZE = 16;
    private final Alloc alloc;
    private final Table table;

    public Versions(final Alloc alloc, final Table table) {
        this.alloc = alloc;
        this.table = table;
    }

    public Versions(final File parent, final Table table) {
        this(new CowAlloc(
                new File(parent, "versions"), PAGE_SIZE),
                table
        );
    }

    @Override
    public Location put(final Version node) {
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream data = new DataOutputStream(bytes);
            data.writeLong(node.timestamp()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
            data.writeLong(node.offset());
            return new LongLocation(
                    this.alloc.allocOne(
                            new DataInputStream(
                                    new ByteArrayInputStream(bytes.toByteArray())
                            )
                    )
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Version fetch(final long index) {
        try {
            final DataInputStream input = new DataInputStream(this.alloc.fetch(index));
            final long millis = input.readLong(); // time
            final long off = input.readLong();
            final Instant instant = Instant.ofEpochMilli(millis);
            return new TimeVersion(
                    off,
                    LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(final Iterable<Long> indexes) {
        try {
            final List<Long> nodes = new LinkedList<>();
            for (final Long index : indexes) {
                final DataInputStream input = new DataInputStream(this.alloc.fetch(index));
                input.readLong(); // time
                final long off = input.readLong();
                nodes.add(off);
            }
            table.nodes().free(nodes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete() {
        try {
            this.alloc.clear();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Location last() {
        try {
            return new LongLocation(alloc.last());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<Location> iterator() {
        return StreamSupport.stream(alloc.spliterator(), false)
                .map(e -> (Location) new LongLocation(e))
                .iterator();
    }
}
