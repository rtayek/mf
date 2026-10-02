package com.tayek.mf;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Mf extends Application {
    @Override
    public void start(Stage stage) {
        BoardView boardView = new BoardView(19, 19);
        StackPane root = new StackPane(boardView);
        root.setPadding(new Insets(0, 0, 28, 0));
        Scene scene = new Scene(root, 760, 820);

        boardView.widthProperty().bind(root.widthProperty().subtract(root.getPadding().getLeft() + root.getPadding().getRight()));
        boardView.heightProperty().bind(root.heightProperty().subtract(root.getPadding().getTop() + root.getPadding().getBottom()));

        stage.setTitle("mf - Go Viewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
