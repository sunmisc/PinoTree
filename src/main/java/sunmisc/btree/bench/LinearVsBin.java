package sunmisc.btree.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sunmisc.btree.LearnedModel;

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
    private LearnedModel linearModel;

    @Setup
    public void prepare() {
        final Random random = new Random(85312);
        keys = LongStream.range(0, 256)
                .map(e -> e + random.nextInt(0, spread))
                .sorted()
                .distinct()
                .boxed()
                .toList();
        linearModel = LearnedModel.retrain(keys);
    }

    @Benchmark
    public int linearSearch() {
        final int r = ThreadLocalRandom.current().nextInt(keys.size());
        return linearModel.searchEq(keys, keys.get(r));
    }

    @Benchmark
    public int binSearch() {
        final int r = ThreadLocalRandom.current().nextInt(keys.size());
        return Collections.binarySearch(keys, keys.get(r));
    }

    @Benchmark
    public int randlinearSearch() {
        final int r = ThreadLocalRandom.current().nextInt(keys.size());
        return linearModel.search(keys, r);
    }

    @Benchmark
    public int randBinSearch() {
        final long r = ThreadLocalRandom.current().nextInt(keys.size());
        return Collections.binarySearch(keys, r);
    }
}
