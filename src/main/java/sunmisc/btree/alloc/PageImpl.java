package sunmisc.btree.alloc;

import sunmisc.btree.api.Page;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public final class PageImpl implements Page {
    private final long offset;
    private final int size;
    private final File file;

    public PageImpl(final long offset, final int size, final File file) {
        this.offset = offset;
        this.size = size;
        this.file = file;
    }

    @Override
    public void write(final InputStream buffer) throws IOException {
        // todo:
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            final byte[] bytes = buffer.readAllBytes();
            if (bytes.length > this.size) {
                throw new IllegalArgumentException(String.format(
                        "stream size is larger than page size %s > %s",
                        bytes.length, this.size
                ));
            }
            raf.seek(this.offset);
            raf.write(bytes);
        }
    }

    @Override
    public InputStream read() throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            return new ByteBufferBackedInputStream(
                    raf.getChannel().map(
                            FileChannel.MapMode.READ_ONLY,
                            this.offset,
                            this.size
                    )
            );
        }
    }

    @Override
    public long offset() {
        return this.offset;
    }

    private static final class ByteBufferBackedInputStream extends InputStream {
        private final ByteBuffer buffer;

        public ByteBufferBackedInputStream(final ByteBuffer buf) {
            this.buffer = buf;
        }

        @Override
        public int available() {
            return this.buffer.remaining();
        }

        @Override
        public int read() {
            return this.buffer.hasRemaining() ? (this.buffer.get() & 0xFF) : -1;
        }

        @Override
        public int read(final byte[] bytes, final int off, int len) {
            if (!this.buffer.hasRemaining()) {
                return -1;
            }
            len = Math.min(len, this.buffer.remaining());
            this.buffer.get(bytes, off, len);
            return len;
        }
    }
}
