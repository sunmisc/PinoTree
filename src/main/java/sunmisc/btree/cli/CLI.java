package sunmisc.btree.cli;

import sunmisc.btree.api.Location;
import sunmisc.btree.objects.Table;
import sunmisc.btree.impl.MutBtree;

import java.util.*;

public final class CLI {
    private static final Map<String, MutBtree> trees = new HashMap<>();
    private static MutBtree currentTree = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Simple DB started. Commands: use, put, get, delete, first, last, list, exit.");
        runCommandLoop();
    }

    private static void runCommandLoop() {
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            long startTime = System.nanoTime();
            try {
                boolean shouldExit = processCommand(command, parts);
                if (shouldExit) break;
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
            long endTime = System.nanoTime();
            System.out.printf("Execution time: %.3f ms%n", (endTime - startTime) / 1_000_000.0);
        }
    }

    private static boolean processCommand(String command, String[] parts) {
        return switch (command) {
            case "use" -> {
                handleUseCommand(parts);
                yield false;
            }
            case "put" -> {
                handlePutCommand(parts);
                yield false;
            }
            case "get" -> {
                handleGetCommand(parts);
                yield false;
            }
            case "delete" -> {
                handleDeleteCommand(parts);
                yield false;
            }
            case "first" -> {
                handleFirstCommand();
                yield false;
            }
            case "last" -> {
                handleLastCommand();
                yield false;
            }
            case "list" -> {
                handleListCommand();
                yield false;
            }
            case "exit" -> {
                System.out.println("Bye!");
                yield true;
            }
            default -> {
                System.out.println("Unknown command: " + command);
                yield false;
            }
        };
    }

    private static void handleUseCommand(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: use <table_name>");
            return;
        }
        String tableName = parts[1];
        currentTree = trees.computeIfAbsent(tableName, name -> {
            Table table = new Table(name);
            Location root;
            try {
                root = table.roots().fetch(table.roots().last().offset());
            } catch (Exception e) {
                System.out.println("Table '" + name + "' is empty. Creating new tree...");
                root = null;
            }
            return new MutBtree(table, root);
        });
        System.out.println("Switched to tree for table '" + tableName + "'.");
    }

    private static void handlePutCommand(String[] parts) {
        checkTree(currentTree);
        if (parts.length < 3) {
            System.out.println("Usage: put <key> <value>");
            return;
        }
        currentTree.put(parseKey(parts[1]), parts[2]);
        System.out.println("OK");
    }

    private static void handleGetCommand(String[] parts) {
        checkTree(currentTree);
        if (parts.length < 2) {
            System.out.println("Usage: get <key>");
            return;
        }
        Optional<String> value = currentTree.get(parseKey(parts[1]));
        System.out.println(value.orElse("NOT FOUND"));
    }

    private static void handleDeleteCommand(String[] parts) {
        checkTree(currentTree);
        if (parts.length < 2) {
            System.out.println("Usage: delete <key>");
            return;
        }
        currentTree.delete(parseKey(parts[1]));
        System.out.println("OK");
    }

    private static void handleFirstCommand() {
        checkTree(currentTree);
        Optional<Map.Entry<Long, String>> first = currentTree.first();
        System.out.println(first.map(e -> e.getKey() + " -> " + e.getValue()).orElse("EMPTY"));
    }

    private static void handleLastCommand() {
        checkTree(currentTree);
        Optional<Map.Entry<Long, String>> last = currentTree.last();
        System.out.println(last.map(e -> e.getKey() + " -> " + e.getValue()).orElse("EMPTY"));
    }

    private static void handleListCommand() {
        checkTree(currentTree);
        currentTree.iterator().forEachRemaining(
                e -> System.out.println(e.getKey() + " -> " + e.getValue())
        );
    }

    private static void checkTree(MutBtree tree) {
        if (tree == null) {
            throw new IllegalStateException("No tree selected. Use 'new <table>' or 'use <table>'.");
        }
    }

    private static Long parseKey(String key) {
        try {
            return Long.parseLong(key);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Key must be a valid number.");
        }
    }
}