package sunmisc.btree.alloc;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import sunmisc.btree.api.Page;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
}
