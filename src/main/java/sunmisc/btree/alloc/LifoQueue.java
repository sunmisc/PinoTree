package sunmisc.btree.alloc;

import sunmisc.btree.api.Location;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.OptionalLong;

public final class LifoQueue {
    private final File origin;

    public LifoQueue(final File origin) {
        this.origin = origin;
    }

    public OptionalLong poll() throws IOException {
        if (!this.origin.exists()) {
            return OptionalLong.empty();
        }
        try (final RandomAccessFile raf = new RandomAccessFile(this.origin, "rw")) {
            final long tail = raf.length();
            if (tail == 0) {
                return OptionalLong.empty();
            }
            final long newTail = tail - Long.BYTES;
            raf.seek(newTail);
            final long value = raf.readLong();
            raf.setLength(newTail);
            return OptionalLong.of(value);
        }
    }

    public void addAll(final Iterable<Location> indexes) throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.origin, "rw")) {
            final long tail = raf.length();
            for (final Location idx : indexes) {
                raf.seek(tail);
                raf.writeLong(idx.offset());
            }
        }
    }

    public void add(final Location index) throws IOException {
        this.addAll(List.of(index));
    }

    public void clear() throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.origin, "rw")) {
            raf.setLength(0);
        }
    }
}
