package sunmisc.btree.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sunmisc.btree.regression.LongRegressionSearch;
import sunmisc.btree.regression.RegressionSearch;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(1)
@Threads(1)
@BenchmarkMode({Mode.Throughput})
@State(Scope.Thread)
public class LinearVsBin {

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(LinearVsBin.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @Param({"1", "2", "32", "64", "1024", "4096"})
    private int spread;
    private List<Long> keys;
    private RegressionSearch<Long> search;

    @Setup
    public void prepare() {
        final Random random = new Random(85312);
        keys = random.longs(10_000, 0, spread)
                .distinct()
                .sorted()
                .boxed()
                .toList();
        search = new LongRegressionSearch().addAll(keys);
    }

    @Benchmark
    public int binarySearch() {
        final int r = ThreadLocalRandom.current().nextInt(keys.size());
        return Collections.binarySearch(keys, keys.get(r));
    }

    @Benchmark
    public int binarySearchRandom() {
        final long r = ThreadLocalRandom.current().nextInt(keys.size());
        return Collections.binarySearch(keys, r);
    }

    @Benchmark
    public int searchWithRegression() {
        final int r = ThreadLocalRandom.current().nextInt(keys.size());
        return search.search(keys, keys.get(r));
    }

    @Benchmark
    public int searchRandomWithRegression() {
        final long r = ThreadLocalRandom.current().nextInt(keys.size());
        return search.search(keys, r);
    }
}
