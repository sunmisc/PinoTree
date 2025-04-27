package sunmisc.btree.decode;

import sunmisc.btree.alloc.CowAlloc;
import sunmisc.btree.api.Alloc;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.Objects;

import java.io.*;
import java.util.Map;

public final class Values implements Objects<Map.Entry<Long, String>> {
    private static final int PAGE_SIZE = 1024;
    private final Alloc alloc;

    public Values(File parent) {
        this(new CowAlloc(
                new File(parent, "values"),
                PAGE_SIZE)
        );
    }

    public Values(Alloc alloc) {
        this.alloc = alloc;
    }

    @Override
    public Location alloc(Map.Entry<Long, String> value) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeLong(value.getKey());
            data.writeUTF(value.getValue());
            long off = alloc.allocOne(new ByteArrayInputStream(out.toByteArray()));
            return () -> off;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map.Entry<Long, String> fetch(long index) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(
                alloc.fetch(index).readAllBytes());
             DataInputStream data = new DataInputStream(in)) {
            return Map.entry(data.readLong(), data.readUTF());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free(Iterable<Long> indexes) {
        try {
            alloc.free(indexes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete() {

    }
}
