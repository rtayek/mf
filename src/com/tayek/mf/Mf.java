package com.tayek.mf;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Mf extends Application {
    @Override
    public void start(Stage stage) {
        BoardPosition position = new BoardPosition(19, 19);
        BoardView boardView = new BoardView(position);
        Label status = new Label();
        status.setStyle("-fx-font-family: serif; -fx-font-size: 16px;");
        BorderPane.setAlignment(status, Pos.CENTER);
        BorderPane.setMargin(status, new Insets(4, 0, 6, 0));

        Runnable updateStatus = () -> status.setText(
                position.sideToMove() == Stone.BLACK ? "Black to move" : "White to move");
        boardView.setOnPositionChanged(updateStatus);
        updateStatus.run();

        BorderPane root = new BorderPane();
        root.setCenter(boardView);
        root.setBottom(status);
        Scene scene = new Scene(root, 760, 800);

        boardView.widthProperty().bind(root.widthProperty());
        boardView.heightProperty().bind(root.heightProperty().subtract(28));

        stage.setTitle("mf - Go Viewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
