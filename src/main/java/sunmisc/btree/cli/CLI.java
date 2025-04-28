package sunmisc.btree.cli;

import sunmisc.btree.api.Location;
import sunmisc.btree.decode.Table;
import sunmisc.btree.impl.MutBtree;

import java.util.*;

public class CLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, MutBtree> trees = new HashMap<>();
        MutBtree currentTree = null;

        System.out.println("Simple DB started. Commands: use, put, get, delete, first, last, list, exit.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "use":
                        if (parts.length < 2) {
                            System.out.println("Usage: use <table_name>");
                        } else {
                            String tableName = parts[1];
                            currentTree = trees.computeIfAbsent(
                                    tableName,
                                    name -> {
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
                        break;

                    case "put":
                        checkTree(currentTree);
                        if (parts.length < 3) {
                            System.out.println("Usage: put <key> <value>");
                        } else {
                            currentTree.put(parseKey(parts[1]), parts[2]);
                            System.out.println("OK");
                        }
                        break;

                    case "get":
                        checkTree(currentTree);
                        if (parts.length < 2) {
                            System.out.println("Usage: get <key>");
                        } else {
                            Optional<String> value = currentTree.get(parseKey(parts[1]));
                            System.out.println(value.orElse("NOT FOUND"));
                        }
                        break;

                    case "delete":
                        checkTree(currentTree);
                        if (parts.length < 2) {
                            System.out.println("Usage: delete <key>");
                        } else {
                            currentTree.delete(parseKey(parts[1]));
                            System.out.println("OK");
                        }
                        break;

                    case "first":
                        checkTree(currentTree);
                        Optional<Map.Entry<Long, String>> first = currentTree.first();
                        System.out.println(first.map(e -> e.getKey() + " -> " + e.getValue()).orElse("EMPTY"));
                        break;

                    case "last":
                        checkTree(currentTree);
                        Optional<Map.Entry<Long, String>> last = currentTree.last();
                        System.out.println(last.map(e -> e.getKey() + " -> " + e.getValue()).orElse("EMPTY"));
                        break;

                    case "list":
                        checkTree(currentTree);
                        currentTree.iterator().forEachRemaining(
                                e -> System.out.println(e.getKey() + " -> " + e.getValue())
                        );
                        break;

                    case "exit":
                        System.out.println("Bye!");
                        return;

                    default:
                        System.out.println("Unknown command: " + command);
                        break;
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
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