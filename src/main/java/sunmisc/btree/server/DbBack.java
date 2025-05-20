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

    public DbBack(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        new DbBack().start(Exit.NEVER);
    }

    public void start(Exit exit) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
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
            """, port);
            while (!exit.ready()) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String command = in.readLine();
                    if (command == null) {
                        continue;
                    }
                    handle(command, out);
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void handle(String query, PrintWriter out) {
        String[] parts = query.split(" ");
        String command = parts[0];
        String table = parts[1];
        Tree<Long, String> tree = tables.get(table);
        Map<String, Operation> ops = Stream.of(
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
        List<String> params = List.of(parts).subList(2, parts.length);

        new ErrorHandleOperation(
                System.out,
                ops.getOrDefault(command, new EmptyOperation())
        ).apply(params);
    }
}
