package sunmisc.btree.objects;

import sunmisc.btree.alloc.TextLocation;
import sunmisc.btree.api.Entry;
import sunmisc.btree.api.Location;
import sunmisc.btree.api.ValueLocation;

public final class OEntry implements Entry {
    private final long key;
    private final String value;
    private final Location address;

    public OEntry(final long key, final String value, final Location address) {
        this.key = key;
        this.value = value;
        this.address = address;
    }

    @Override
    public long key() {
        return this.key;
    }

    @Override
    public ValueLocation value() {
        return new TextLocation(this.value, this.address);
    }
}