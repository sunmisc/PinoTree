package sunmisc.btree.objects;

import sunmisc.btree.api.Version;

import java.time.LocalDateTime;

public final class TimeVersion implements Version {
    private final long offset;
    private final LocalDateTime timestamp;

    public TimeVersion(final long offset) {
        this(offset, LocalDateTime.now());
    }

    public TimeVersion(final long offset, final LocalDateTime timestamp) {
        this.offset = offset;
        this.timestamp = timestamp;
    }

    @Override
    public LocalDateTime timestamp() {
        return this.timestamp;
    }

    @Override
    public long offset() {
        return this.offset;
    }
}
