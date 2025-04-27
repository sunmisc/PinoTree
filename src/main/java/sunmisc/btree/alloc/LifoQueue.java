package sunmisc.btree.alloc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
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
        try (final RandomAccessFile raf = new RandomAccessFile(this.origin, "rw");
             final FileChannel channel = raf.getChannel();
             final FileLock lock = channel.lock()) {

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
    public void addAll(final Iterable<Long> indexes) throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.origin, "rw");
             final FileChannel channel = raf.getChannel();
             final FileLock lock = channel.lock()) {
            for (final long idx : indexes) {
                final long tail = raf.length();
                raf.seek(tail);
                raf.writeLong(idx);
            }
        }
    }

    public void add(final long index) throws IOException {
        this.addAll(List.of(index));
    }

    public void clear() throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.origin, "rw")) {
            raf.setLength(0);
        }
    }
}
