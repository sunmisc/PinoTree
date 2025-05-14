module simple.btree.db {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires com.github.benmanes.caffeine;

    opens sunmisc.btree.ui to javafx.fxml;
    exports sunmisc.btree.ui;
}