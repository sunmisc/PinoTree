package sunmisc.btree.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface Page extends Location {

    void write(InputStream buffer) throws IOException;

    InputStream read() throws FileNotFoundException, IOException;
}
