package sunmisc.btree;

import sunmisc.btree.decode.Table;
import sunmisc.btree.impl.MutBtree;

import java.io.IOException;

public class Test {
    public static void main(final String[] args) throws IOException {
        Table table = new Table("test");
        final MutBtree btree = new MutBtree(table);
        for (long i = 10; i < 20; i++) {
            btree.put(i, i+"");
        }
        final MutBtree tree = new MutBtree(table, table.roots().fetch(table.roots().last().offset()));
        System.out.println(tree);
    /*    table.roots().iterator().forEachRemaining(root -> {
            final MutBtree tree = new MutBtree(table, table.roots().fetch(root.offset()));
            System.out.println(tree);
            System.out.println("-------------------------------- " + root);
            table.roots().free(root.offset());
        });*/
    }
}