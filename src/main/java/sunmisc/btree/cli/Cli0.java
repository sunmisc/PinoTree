package sunmisc.btree.cli;

import sunmisc.btree.cli.ops.UseOperation;

import java.util.List;
import java.util.Scanner;

public class Cli0 {

    public static void main(String[] args) {
        System.out.println("Simple DB started. Commands: use, put, get, delete, first, last, list, exit.");

        UseOperation start = new UseOperation();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+", 3);
                String command = parts[0].toLowerCase();

                if (command.equals("use")) {
                    start.apply(List.of(parts[1]));
                }
            }
        }
    }
}
