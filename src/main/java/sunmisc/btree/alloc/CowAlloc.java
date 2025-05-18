package sunmisc.btree.alloc;

import sunmisc.btree.api.Alloc;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Page;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.LongStream;

public final class CowAlloc implements Alloc {
    private static final long HEADER_SIZE = Long.BYTES * 2;
    private static final long LAST = 0;
    private static final long CTL  = 8;

    private final File file;
    private final int pageSize;
    private final LifoQueue removals;
    private final Map<Long, Long> header = new HashMap<>();

    public CowAlloc(final File file, final int pageSize) {
        this.file = file;
        this.removals = new LifoQueue(new File(
                file.getParentFile(),
                String.format(
                        "%s_removals.dat",
                        file.getName()
                )
        ));
        this.pageSize = pageSize;
    }

    @Override
    public void free(final Iterable<Location> indexes) throws IOException {
        for (final Location offset : indexes) {
            if ((offset.offset() - HEADER_SIZE) % this.pageSize != 0) {
                throw new IllegalArgumentException(String.format(
                        "offset %s must be aligned to %s",
                        offset,
                        this.pageSize
                ));
            }
        }
        this.removals.addAll(indexes);
    }

    @Override
    public Page alloc() throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            final long index;
            final OptionalLong polled = this.removals.poll();
            if (polled.isPresent()) {
                index = polled.getAsLong();
            } else {
                index = this.tail();
                header.put(CTL, index + pageSize);
            }
            header.put(LAST, index);
            updateHeader(raf);
            return new PageImpl(index, this.pageSize, this.file);
        }
    }

    private void updateHeader(RandomAccessFile raf) throws IOException {
        raf.seek(0);
        raf.writeLong(header.get(LAST));
        raf.writeLong(header.get(CTL));
    }

    @Override
    public Page fetch(final Location offset) throws IOException {
        if ((offset.offset() - HEADER_SIZE) % this.pageSize != 0) {
            throw new IllegalArgumentException(String.format(
                    "offset %s must be aligned to %s",
                    offset, this.pageSize
            ));
        }
        return new PageImpl(offset.offset(), this.pageSize, this.file);
    }

    @Override
    public void clear() throws IOException {
        this.removals.clear();
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            raf.setLength(0);
            header.remove(LAST);
            header.remove(CTL);
        }
    }

       private long tail() {
        return header.computeIfAbsent(CTL, off -> {
            try (final RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(off);
                return raf.readLong();
            } catch (final IOException ex) {
                return HEADER_SIZE;
            }
        });
    }

    @Override
    public long last() {
        return header.computeIfAbsent(LAST, off -> {
            try (final RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(off);
                return raf.readLong();
            } catch (final IOException ex) {
                throw new RuntimeException("empty");
            }
        });
    }

    private long size() {
        return Math.divideExact(tail() - HEADER_SIZE, this.pageSize);
    }

    @Override
    public Iterator<Long> iterator() {
        final long size = size() * Long.BYTES;
        return LongStream.iterate( Long.BYTES,
                value -> value <= size,
                operand -> operand + pageSize
        ).iterator();
    }
}
