package sunmisc.btree.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sunmisc.btree.impl.LeafNode;
import sunmisc.btree.impl.MutBtree;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(1)
@Threads(1)
@BenchmarkMode({Mode.Throughput})
public class BTreeBench {
    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(BTreeBench.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
    @Param({"true", "false"})
    private boolean learned;
    private static final int MAX = 100;
    private MutBtree bTree;

    @Setup
    public void prepare() {
        this.bTree = new MutBtree();
        LeafNode.LEARN_MODEL = learned;
        for (long i = 0; i < MAX; ++i) {
            this.bTree.put(i, i + "");
        }
    }

    @Benchmark
    public Optional<String> read() {
        final long r = ThreadLocalRandom.current().nextInt(MAX);
        return this.bTree.get(r);
    }
}
