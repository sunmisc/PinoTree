package sunmisc.btree;

import sunmisc.btree.impl.BTree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        Map<Long, String> map = new HashMap<>();
        BTree bTree = new BTree();
        for (long i = 0; i < 1000; ++i) {
            map.put(i, i + "");
            bTree.insert(i, i + "");
        }
        bTree.print();
        for (long i = 0; i < 64; i++) {
            System.out.println(map.get(i) + " " + bTree.search(i));
        }

        for (long i = 0; i < 64; i += 3) {
            map.remove(i);
            bTree.delete(i);

            map.put(i + 2, i + 2 + "");
            bTree.insert(i + 2, i + 2 + "");
        }

        System.out.println("REMOVED----------------------------");
        for (long i = 0; i < 64; i++) {
            System.out.println(map.get(i) + " " + bTree.search(i));
        }
        for (long i = 0; i < 64; i += 2) {
            map.put(i, i + "");
            bTree.insert(i, i + "");
        }
        for (long i = 0; i < 64; i++) {
            System.out.println(map.get(i) + " " + bTree.search(i));
        }
        bTree.delete();
    }
}