package sunmisc.btree.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
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
                // .syncIterations(false)
                // .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }
    private static final int MAX = 1_00;
    private MutBtree bTree;

    @Setup
    public void prepare() {
        bTree = new MutBtree();
        for (long i = 0; i < MAX; ++i) {
            bTree.put(i, i + "");
        }
    }

    @Benchmark
    public Optional<String> read() {
        long r = ThreadLocalRandom.current().nextInt(MAX);
        return bTree.get(r);
    }
}
