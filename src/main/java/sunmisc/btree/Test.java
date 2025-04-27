package sunmisc.btree;

import sunmisc.btree.impl.MutBtree;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        MutBtree btree = new MutBtree();
        btree.put(1L, "lool");
        System.out.println(btree.get(1L));
    }
}