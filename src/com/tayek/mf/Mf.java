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
        BorderPane.setMargin(status, new Insets(4, 0, 12, 0));

        Runnable updateStatus = () -> {
            String side = position.sideToMove() == Stone.BLACK ? "Black" : "White";
            status.setText("Move " + position.moveNumber() + " / " + position.moveCount() + "    " + side + " to move");
        };
        boardView.setOnPositionChanged(updateStatus);
        updateStatus.run();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0, 0, 8, 0));
        root.setCenter(boardView);
        root.setBottom(status);
        Scene scene = new Scene(root, 760, 800);

        scene.setOnKeyPressed(event -> {
            boolean changed = switch (event.getCode()) {
                case LEFT -> position.previous();
                case RIGHT -> position.next();
                case HOME -> position.first();
                case END -> position.last();
                default -> false;
            };
            if (changed) {
                boardView.refresh();
                event.consume();
            }
        });

        stage.setTitle("mf - Go Viewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
