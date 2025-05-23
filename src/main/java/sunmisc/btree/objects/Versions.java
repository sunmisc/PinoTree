package sunmisc.btree.objects;

import sunmisc.btree.alloc.Malloc;
import sunmisc.btree.alloc.LongLocation;
import sunmisc.btree.api.*;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class Versions implements Objects<Version> {
    private static final int PAGE_SIZE = Long.BYTES * 2;
    private final Alloc alloc;
    private final Table table;

    public Versions(final Alloc alloc, final Table table) {
        this.alloc = alloc;
        this.table = table;
    }

    public Versions(final File parent, final Table table) {
        this(new Malloc(
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
            final Page page = this.alloc.alloc();
            page.write(new DataInputStream(
                    new ByteArrayInputStream(bytes.toByteArray())
            ));
            return page;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Version fetch(final Location index) {
        try {
            final DataInputStream input = new DataInputStream(
                    this.alloc.fetch(index).read());
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
    public void free(final Iterable<Location> indexes) {
        try {
            final List<Location> nodes = new LinkedList<>();
            for (final Location index : indexes) {
                final DataInputStream input = new DataInputStream(
                        this.alloc.fetch(index).read());
                input.readLong(); // time
                final long off = input.readLong();
                nodes.add(new LongLocation(off));
            }
            this.table.nodes().free(nodes);
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
    public Optional<Location> lastIndex() {
        try {
            return Optional.of(new LongLocation(this.alloc.last()));
        } catch (final Exception e) {
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
