package com.tayek.mf;

import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Independent, disposable playback of one KataGo principal variation. */
final class KataGoVariationPreview {
    static void open(GameController.Snapshot snapshot, KataGoAnalysis.Candidate candidate, Stage owner) {
        GameController copy = new GameController();
        copy.openSnapshot(snapshot);
        BoardPosition initial = copy.position();
        List<Move> variation = KataGoVariation.moves(candidate.variation(),
                initial.width(), initial.height(), initial.sideToMove());
        new KataGoVariationPreview(copy, variation, candidate.move()).show(owner);
    }

    private KataGoVariationPreview(GameController copy, List<Move> moves, String candidateName) {
        this.copy = copy;
        this.moves = moves;
        this.candidateName = candidateName;
        board = new BoardView(copy.position());
        playback = new Timeline(new KeyFrame(Duration.millis(850), event -> advance()));
        playback.setCycleCount(Animation.INDEFINITE);
    }

    private void show(Stage owner) {
        Stage window = new Stage();
        window.initOwner(owner);
        window.setTitle("MF - KataGo Variation: " + candidateName);
        Button home = new Button("Home");
        Button back = new Button("Back");
        Button play = new Button("Play");
        Button forward = new Button("Forward");
        Button end = new Button("End");
        Button close = new Button("Close");
        home.setOnAction(e -> { pause(play); step = 0; render(); });
        back.setOnAction(e -> { pause(play); if (step > 0) { step--; render(); } });
        forward.setOnAction(e -> { pause(play); advance(); });
        end.setOnAction(e -> {
            pause(play);
            while (step < moves.size() && advance()) { }
        });
        play.setOnAction(e -> {
            if (playback.getStatus() == Animation.Status.RUNNING) pause(play);
            else {
                if (step == moves.size()) { step = 0; render(); }
                if (!moves.isEmpty()) {
                    playback.play();
                    play.setText("Pause");
                }
            }
        });
        close.setOnAction(e -> window.close());
        playback.setOnFinished(event -> pause(play));
        playButton = play;
        controls = new Button[]{home, back, play, forward, end};
        HBox buttons = new HBox(10, home, back, play, forward, end, close);
        buttons.setAlignment(Pos.CENTER);
        VBox footer = new VBox(8, progress, buttons);
        footer.setPadding(new Insets(8));
        footer.setAlignment(Pos.CENTER);
        BorderPane layout = new BorderPane();
        layout.setCenter(board);
        layout.setBottom(footer);
        window.setScene(new Scene(layout, 760, 810));
        window.setOnHidden(e -> playback.stop());
        render();
        window.show();
    }

    private boolean advance() {
        if (step >= moves.size()) { playback.stop(); return false; }
        int previous = step++;
        try {
            render();
            if (step == moves.size()) {
                playback.stop();
                if (playButton != null) playButton.setText("Play");
            }
            return true;
        } catch (IllegalArgumentException ex) {
            step = previous;
            playback.stop();
            if (playButton != null) playButton.setText("Play");
            progress.setText("Cannot play move " + (step + 1) + ": " + ex.getMessage());
            return false;
        }
    }

    private void render() {
        BoardPosition position = copy.position();
        for (int i = 0; i < step; i++) {
            Move move = moves.get(i);
            boolean legal = move.x() < 0 ? position.pass() : position.play(move.x(), move.y());
            if (!legal) throw new IllegalArgumentException("illegal predicted move " + move);
        }
        board.setPosition(position);
        progress.setText("Candidate " + candidateName + "   Move " + step + " / " + moves.size());
        if (controls != null) {
            controls[0].setDisable(step == 0);
            controls[1].setDisable(step == 0);
            controls[3].setDisable(step == moves.size());
            controls[4].setDisable(step == moves.size());
        }
    }

    private void pause(Button button) {
        playback.pause();
        button.setText("Play");
    }

    private final GameController copy;
    private final List<Move> moves;
    private final String candidateName;
    private final BoardView board;
    private final Timeline playback;
    private final Label progress = new Label();
    private Button[] controls;
    private Button playButton;
    private int step;
}
