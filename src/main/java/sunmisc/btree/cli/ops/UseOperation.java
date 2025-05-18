package sunmisc.btree.cli.ops;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import sunmisc.btree.api.Tree;
import sunmisc.btree.impl.MutBtree;
import sunmisc.btree.objects.Table;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class UseOperation implements Operation {
    private final LoadingCache<String, Tree<Long, String>> tables = Caffeine
            .newBuilder()
            .build(table -> new MutBtree(new Table(table)));

    @Override
    public void apply(List<String> args) {
        String table = args.getFirst();
        Tree<Long, String> tree = tables.get(table);
        Map<String, Operation> ops = Stream.of(
                new PutOperation(tree),
                new DeleteOperation(tree),
                new GetOperation(tree, System.out),
                new FirstOperation(tree, System.out),
                new LastOperation(tree, System.out),
                new ListOperation(tree, System.out),
                new RangeOperation(tree, System.out)
        ).collect(Collectors.toUnmodifiableMap(
                Operation::name,
                Function.identity())
        );
        Thread thread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split("\\s+", 3);
                    String command = parts[0].toLowerCase();
                    List<String> params = List.of(parts).subList(1, parts.length);
                    if (command.equals("use")) {
                        apply(params);
                        break;
                    }
                    new TimedOperation(
                            new ErrorHandleOperation(
                                    System.out,
                                    ops.getOrDefault(command, new EmptyOperation())
                            )
                    ).apply(params);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "use";
    }
}
