package sunmisc.btree.cli.ops;

import java.io.PrintStream;
import java.util.List;

public final class ErrorHandleOperation implements Operation {
    private final PrintStream stream;
    private final Operation origin;

    public ErrorHandleOperation(PrintStream stream, Operation origin) {
        this.stream = stream;
        this.origin = origin;
    }

    @Override
    public void apply(List<String> args) {
        try {
            this.origin.apply(args);
        } catch (Exception e) {
            this.stream.println(e.getMessage());
        }
    }

    @Override
    public String name() {
        return origin.name();
    }
}
