module simple.btree.db {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires com.github.benmanes.caffeine;
    requires com.fasterxml.jackson.databind;

    opens sunmisc.btree.ui to javafx.fxml;
    exports sunmisc.btree.ui;
    exports sunmisc.btree.impl;
    exports sunmisc.btree.objects;
}