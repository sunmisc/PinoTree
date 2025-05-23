package sunmisc.btree.server;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import sunmisc.btree.api.Tree;
import sunmisc.btree.impl.MutBtree;
import sunmisc.btree.objects.IOTable;
import sunmisc.btree.ops.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DbBack {
    private static final int DEFAULT_PORT = 8080;
    private final LoadingCache<String, Tree<Long, String>> tables = Caffeine
            .newBuilder()
            .build(table -> new MutBtree(new IOTable(table)));
    private final int port;

    public DbBack() {
        this(DEFAULT_PORT);
    }

    public DbBack(final int port) {
        this.port = port;
    }

    public static void main(final String[] args) {
        new DbBack().start(Exit.NEVER);
    }

    public void start(final Exit exit) {
        try (final ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.printf("""
            Commands:
                   put [table] [key] [value] ... <key_n> <value_n>
                   get [table] [key] ... <key_n>
                   delete [table] [key] ... <key_n>
                   deleteIfMapped [table] [key] [value] ... <key_n> <value_n>
                   first/last [table]
                   list [table] <offset> <count>
                   range [table] [from] [to]
                   size [table]
            Server started on port %s%n
            """, this.port);
            while (!exit.ready()) {
                try (final Socket clientSocket = serverSocket.accept();
                     final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    final String command = in.readLine();
                    if (command == null) {
                        continue;
                    }
                    this.handle(command, out);
                } catch (final IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (final IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void handle(final String query, final PrintWriter out) {
        final String[] parts = query.split(" ");
        final String command = parts[0];
        final String table = parts[1];
        final Tree<Long, String> tree = this.tables.get(table);
        final Map<String, Operation> ops = Stream.of(
                new PutOperation(tree),
                new DeleteOperation(tree),
                new DeleteIfMappedOperation(tree),
                new GetOperation(tree, out),
                new FirstOperation(tree, out),
                new LastOperation(tree, out),
                new ListOperation(tree, out),
                new RangeOperation(tree, out),
                new SizeOperation(tree, out)
        ).collect(Collectors.toUnmodifiableMap(
                Operation::name,
                Function.identity())
        );
        final List<String> params = List.of(parts).subList(2, parts.length);

        new ErrorHandleOperation(
                System.out,
                ops.getOrDefault(command, new EmptyOperation())
        ).apply(params);
    }
}
