package com.tayek.mf;

import java.io.File;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Mf extends Application {
    private BoardPosition position = new BoardPosition(19,19);
    private BoardView boardView;
    private Label status;
    private String gameLabel = "";

    @Override public void start(Stage stage) {
        boardView=new BoardView(position);
        status=new Label(); status.setStyle("-fx-font-family: serif; -fx-font-size: 16px;");
        BorderPane.setAlignment(status,Pos.CENTER); BorderPane.setMargin(status,new Insets(4,0,12,0));
        boardView.setOnPositionChanged(this::updateStatus); updateStatus();

        MenuItem open=new MenuItem("Open SGF..."); open.setOnAction(e->openSgf(stage));
        Menu file=new Menu("File"); file.getItems().add(open); MenuBar menuBar=new MenuBar(file);
        BorderPane content=new BorderPane(); content.setPadding(new Insets(0,0,8,0)); content.setCenter(boardView); content.setBottom(status);
        BorderPane root=new BorderPane(); root.setTop(menuBar); root.setCenter(content);
        Scene scene=new Scene(root,760,800);
        scene.setOnKeyPressed(event->{boolean changed=switch(event.getCode()){case LEFT->position.previous();case RIGHT->position.next();case HOME->position.first();case END->position.last();default->false;};if(changed){boardView.refresh();event.consume();}});
        stage.setTitle("mf - Go Viewer"); stage.setScene(scene); stage.show();
    }

    private void openSgf(Stage stage) {
        FileChooser chooser=new FileChooser(); chooser.setTitle("Open SGF Game"); chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SGF files","*.sgf"));
        File file=chooser.showOpenDialog(stage); if(file==null)return;
        try {
            SgfGame game=SgfReader.read(file.toPath());
            BoardPosition loaded=new BoardPosition(game.boardSize(),game.boardSize()); loaded.loadMoves(game.moves());
            position=loaded; boardView.setPosition(loaded);
            String players=game.blackPlayer()+" - "+game.whitePlayer();
            gameLabel=(game.gameName()==null||game.gameName().isBlank()?players:game.gameName()+"    "+players);
            updateStatus(); stage.setTitle("mf - "+game.gameName());
        } catch(Exception ex) {
            new Alert(Alert.AlertType.ERROR,"Could not load SGF:\n"+ex.getMessage(),ButtonType.OK).showAndWait();
        }
    }

    private void updateStatus() {
        String side=position.sideToMove()==Stone.BLACK?"Black":"White";
        String prefix=gameLabel.isBlank()?"":gameLabel+"    ";
        status.setText(prefix+"Move "+position.moveNumber()+" / "+position.moveCount()+"    "+side+" to move");
    }

    public static void main(String[]args){launch(args);}
}
