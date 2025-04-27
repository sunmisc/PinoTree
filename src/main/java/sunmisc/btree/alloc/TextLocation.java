package sunmisc.btree.alloc;

import sunmisc.btree.api.Location;
import sunmisc.btree.api.ValueLocation;

public record TextLocation(
        String value, Location location
) implements ValueLocation {
    @Override
    public String value() {
        return this.value;
    }

    @Override
    public long offset() {
        return this.location.offset();
    }
}