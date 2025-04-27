package sunmisc.btree.alloc;

import sunmisc.btree.api.Location;

public record LongLocation(long offset) implements Location {
}
