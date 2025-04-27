package sunmisc.btree.alloc;

import sunmisc.btree.api.Alloc;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Stream;

public final class CowAlloc implements Alloc {
    private final File file;
    private final int pageSize;
    private final LifoQueue removals;

    public CowAlloc(File file, int pageSize) {
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

    public static void main(String[] args) throws IOException {
        CowAlloc alloc = new CowAlloc(new File("lol"), 4);

        long off = alloc.allocOne(new ByteArrayInputStream(new byte[]{1,2,3}));
        alloc.free(off);
        System.out.println(off);
        System.out.println("after remove "+off);
        System.out.println(Arrays.toString(alloc.fetch(off).readAllBytes()));

        off = alloc.allocOne(new ByteArrayInputStream(new byte[]{1,2,3,4}));
        System.out.println(Arrays.toString(alloc.fetch(off).readAllBytes()));
        System.out.println("before remove "+off);
      //  alloc.delete();
    }
    @Override
    public void free(Iterable<Long> indexes) throws IOException {
        for (Long offset : indexes) {
            if ((offset - 8) % pageSize != 0) {
                throw new IllegalArgumentException(String.format(
                        "offset %s must be aligned to %s",
                        offset,
                        pageSize
                ));
            }
        }
        removals.addAll(indexes);
    }

    @Override
    public long allocOne(InputStream input) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            byte[] bytes = input.readAllBytes();
            if (bytes.length > pageSize) {
                throw new IllegalArgumentException(String.format(
                        "stream size is larger than page size %s > %s",
                        bytes.length, pageSize
                ));
            }
            long index;
            OptionalLong polled = removals.poll();
            if (polled.isPresent()) {
                index = polled.getAsLong();
                write(raf, index, bytes);
            } else {
                index = last();
                write(raf, index, bytes);

                raf.seek(0);
                raf.writeLong(index + pageSize);
            }
            return index;
        }
    }

    @Override
    public Stream<Long> alloc(InputStream input) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long offset = last();
            List<Long> pages = alloc0(raf, offset, input);
            raf.seek(0);
            raf.writeLong(offset + ((long) pages.size() * pageSize));
            return pages.stream();
        }
    }

    @Override
    public InputStream fetch(long offset) throws IOException {
        if ((offset - 8) % pageSize != 0) {
            throw new IllegalArgumentException(String.format(
                    "offset %s must be aligned to %s",
                    offset, pageSize
            ));
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            // todo: channel
            byte[] bytes = new byte[pageSize];
            raf.read(bytes);
            return new ByteArrayInputStream(bytes);
        }
    }

    @Override
    public void clear() throws IOException {
        for (long i = 8; i < last(); i += pageSize) {
            removals.add(i);
        }
    }

    private List<Long> alloc0(RandomAccessFile raf, long pos, InputStream input) throws IOException {
        byte[] bytes = input.readAllBytes();
        int pages = Math.ceilDiv(bytes.length, pageSize);
        List<Long> accumulate = new ArrayList<>(pages);
        long offset = pos;
        for (int i = 0; i < pages; ++i) {
            int start = i * pageSize;
            write(raf, offset, new ByteArrayInputStream(
                    bytes,
                    start,
                    Math.min(bytes.length, pageSize))
            );
            accumulate.add(offset);
            offset += pageSize;
        }
        return accumulate;
    }
    private void write(RandomAccessFile raf, long pos, byte[] bytes) throws IOException {
        raf.seek(pos);
        raf.write(bytes);
    }
    private void write(RandomAccessFile raf, long pos, InputStream stream) throws IOException {
        final byte[] bytes = new byte[pageSize];
        stream.read(bytes);
        write(raf, pos, bytes);
    }

    public long last() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            return raf.readLong();
        } catch (IOException ex) {
            return 8;
        }
    }

    public int size() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            return Math.toIntExact((raf.readLong() - 8) / pageSize);
        } catch (IOException ex) {
            return 0;
        }
    }
}
