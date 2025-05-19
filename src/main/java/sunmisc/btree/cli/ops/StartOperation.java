package sunmisc.btree.cli.ops;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import sunmisc.btree.api.Tree;
import sunmisc.btree.impl.MutBtree;
import sunmisc.btree.objects.IOTable;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StartOperation implements Operation {
    private final LoadingCache<String, Tree<Long, String>> tables = Caffeine
            .newBuilder()
            .build(table -> new MutBtree(new IOTable(table)));

    @Override
    public void apply(List<String> args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(" ");
                String command = parts[0];
                String table = parts[1];
                Tree<Long, String> tree = tables.get(table);
                Map<String, Operation> ops = Stream.of(
                        new PutOperation(tree),
                        new DeleteOperation(tree),
                        new DeleteIfMappedOperation(tree),
                        new GetOperation(tree, System.out),
                        new FirstOperation(tree, System.out),
                        new LastOperation(tree, System.out),
                        new ListOperation(tree, System.out),
                        new RangeOperation(tree, System.out),
                        new SizeOperation(tree, System.out)
                ).collect(Collectors.toUnmodifiableMap(
                        Operation::name,
                        Function.identity())
                );
                List<String> params = List.of(parts).subList(2, parts.length);
                new TimedOperation(
                        new ErrorHandleOperation(
                                System.out,
                                ops.getOrDefault(command, new EmptyOperation())
                        )
                ).apply(params);
            }
        }
    }

    @Override
    public String name() {
        return "start";
    }
}
