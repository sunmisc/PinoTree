package sunmisc.btree.api;

import java.time.LocalDateTime;

public interface Version extends Location {

    LocalDateTime timestamp();
}
