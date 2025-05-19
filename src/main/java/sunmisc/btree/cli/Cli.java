package sunmisc.btree.cli;

import sunmisc.btree.cli.ops.StartOperation;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

public class Cli {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("""
                Diplom database
                Commands:
                   put <table> <key> <value> ... <key_n> <value_n>
                   get <table> <key> ... <key_n>
                   delete <table> <key> ... <key_n>
                   deleteIfMapped <table> <key> <value> ... <key_n> <value_n>
                   first/last <table>
                   list <table>
                   range <table> <from> <to>
                   size <table>
                """);
        StartOperation start = new StartOperation();
/*        StringJoiner joiner = new StringJoiner(" ", "put test ", "");
        for (int i = 0; i < 100_000; i++) {
            joiner.add(i +" " +i);
        }
        String simulatedInput = joiner.toString();
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));*/

        start.apply(List.of());
    }
}
