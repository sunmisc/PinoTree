package sunmisc.btree.ops;

import java.util.List;

public final class TimedOperation implements Operation {
    private final Operation origin;

    public TimedOperation(final Operation origin) {
        this.origin = origin;
    }

    @Override
    public void apply(final List<String> args) {
        final long start = System.currentTimeMillis();
        this.origin.apply(args);
        final long end = System.currentTimeMillis();
        System.out.printf("Time: %sms%n", (end - start));
    }

    @Override
    public String name() {
        return this.origin.name();
    }
}
