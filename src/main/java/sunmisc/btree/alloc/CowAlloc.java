package sunmisc.btree.alloc;

import sunmisc.btree.api.Alloc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Stream;

public final class CowAlloc implements Alloc {
    private final File file;
    private final int pageSize;
    private final LifoQueue removals;

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
    public void free(final Iterable<Long> indexes) throws IOException {
        for (final Long offset : indexes) {
            if ((offset - 8) % this.pageSize != 0) {
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
    public long allocOne(final InputStream input) throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            final byte[] bytes = input.readAllBytes();
            if (bytes.length > this.pageSize) {
                throw new IllegalArgumentException(String.format(
                        "stream size is larger than page size %s > %s",
                        bytes.length, this.pageSize
                ));
            }
            final long index;
            final OptionalLong polled = this.removals.poll();
            if (polled.isPresent()) {
                index = polled.getAsLong();
                this.write(raf, index, bytes);
            } else {
                index = this.last();
                this.write(raf, index, bytes);

                raf.seek(0);
                raf.writeLong(index + this.pageSize);
            }
            return index;
        }
    }

    @Override
    public Stream<Long> alloc(final InputStream input) throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            final long offset = this.last();
            final List<Long> pages = this.alloc0(raf, offset, input);
            raf.seek(0);
            raf.writeLong(offset + ((long) pages.size() * this.pageSize));
            return pages.stream();
        }
    }

    @Override
    public InputStream fetch(final long offset) throws IOException {
        if ((offset - 8) % this.pageSize != 0) {
            throw new IllegalArgumentException(String.format(
                    "offset %s must be aligned to %s",
                    offset, this.pageSize
            ));
        }
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "r")) {
            raf.seek(offset);
            // todo: channel
            final byte[] bytes = new byte[this.pageSize];
            raf.read(bytes);
            return new ByteArrayInputStream(bytes);
        }
    }

    @Override
    public void clear() throws IOException {
        for (long i = 8; i < this.last(); i += this.pageSize) {
            this.removals.add(i);
        }
    }

    private List<Long> alloc0(final RandomAccessFile raf, final long pos, final InputStream input) throws IOException {
        final byte[] bytes = input.readAllBytes();
        final int pages = Math.ceilDiv(bytes.length, this.pageSize);
        final List<Long> accumulate = new ArrayList<>(pages);
        long offset = pos;
        for (int i = 0; i < pages; ++i) {
            final int start = i * this.pageSize;
            this.write(raf, offset, new ByteArrayInputStream(
                    bytes,
                    start,
                    Math.min(bytes.length, this.pageSize))
            );
            accumulate.add(offset);
            offset += this.pageSize;
        }
        return accumulate;
    }
    private void write(final RandomAccessFile raf, final long pos, final byte[] bytes) throws IOException {
        raf.seek(pos);
        raf.write(bytes);
    }
    private void write(final RandomAccessFile raf, final long pos, final InputStream stream) throws IOException {
        final byte[] bytes = new byte[this.pageSize];
        stream.read(bytes);
        this.write(raf, pos, bytes);
    }

    public long last() {
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "r")) {
            return raf.readLong();
        } catch (final IOException ex) {
            return 8;
        }
    }

    public int size() {
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "r")) {
            return Math.toIntExact((raf.readLong() - 8) / this.pageSize);
        } catch (final IOException ex) {
            return 0;
        }
    }
}
