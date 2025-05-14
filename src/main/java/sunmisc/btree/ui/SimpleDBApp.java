package sunmisc.btree.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sunmisc.btree.api.Tree;
import sunmisc.btree.impl.MutBtree;
import sunmisc.btree.objects.Table;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimpleDBApp extends Application {
    public static final Map<String, Tree<Long, String>> trees = new HashMap<>();
    public static Tree<Long, String> currentTree = null;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SimpleDBApp.class.getResource("/SimpleDB.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Simple B-Tree Database");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void switchTree(String tableName) {
        currentTree = trees.computeIfAbsent(tableName, name -> {
            Table table = new Table(name);
            return new MutBtree(table);
        });
    }
}