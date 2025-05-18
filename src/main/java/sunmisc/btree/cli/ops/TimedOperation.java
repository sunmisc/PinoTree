package sunmisc.btree.cli.ops;

import java.util.List;

public final class TimedOperation implements Operation {
    private final Operation origin;

    public TimedOperation(Operation origin) {
        this.origin = origin;
    }

    @Override
    public void apply(List<String> args) {
        final long start = System.currentTimeMillis();
        this.origin.apply(args);
        final long end = System.currentTimeMillis();
        System.out.printf("Time: %sms%n", (end - start));
    }

    @Override
    public String name() {
        return origin.name();
    }
}
