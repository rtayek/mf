package com.tayek.mf;

import java.nio.file.Path;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Separate, read-only analysis panel for an independent GameController snapshot. */
final class KataGoPanel {
    static void open(GameController controller, Stage owner, BoardView board) {
        KataGoPanel panel = new KataGoPanel();
        panel.board = board;
        Stage window = new Stage();
        window.initOwner(owner);
        window.setTitle("MF - KataGo Analysis");
        Button start = new Button("Start Auto Analysis");
        Button stop = new Button("Stop");
        Button close = new Button("Close");
        close.setOnAction(e -> window.close());
        stop.setDisable(true);
        CheckBox showHeatmap = new CheckBox("Show candidate moves on board");
        showHeatmap.setSelected(true);
        showHeatmap.setOnAction(e -> board.setHeatmap(showHeatmap.isSelected() ? panel.lastMoves : java.util.List.of()));
        Label status = new Label("Select a Go position, then start analysis");
        TextArea result = new TextArea();
        result.setEditable(false);
        result.setStyle("-fx-font-family: monospace; -fx-font-size: 16px;");
        ListView<String> candidates = new ListView<>();
        candidates.setStyle("-fx-font-family: monospace; -fx-font-size: 16px;");
        candidates.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2 || panel.positionSnapshot == null) return;
            int index = candidates.getSelectionModel().getSelectedIndex();
            if (index < 0 || index >= panel.currentCandidates.size()) return;
            try {
                KataGoVariationPreview.open(panel.positionSnapshot, panel.currentCandidates.get(index), window);
            } catch (RuntimeException ex) {
                status.setText("Could not preview variation: " + ex.getMessage());
            }
        });
        panel.candidatesList = candidates;
        result.setPrefRowCount(3);
        VBox layout = new VBox(12, new HBox(12, start, stop, close), showHeatmap,
                new Label("Green = higher search rank; larger circles = more visits"), status,
                new Label("Double-click a candidate to preview its variation"), candidates, result);
        layout.setPadding(new Insets(12));
        VBox.setVgrow(candidates, Priority.ALWAYS);
        window.setScene(new Scene(layout, 630, 440));
        panel.latest = new LatestAnalysis<>(Platform::runLater, (analysis, error) -> {
            if (!panel.running) return;
            if (error != null) {
                panel.clearHeatmap();
                panel.clearPreview();
                status.setText("Analysis failed");
                result.setText(error.getCause() == null ? error.toString() : error.getCause().toString());
            } else {
                panel.lastMoves = KataGoHeatmap.candidates(analysis, board.position().width(), board.position().height());
                board.setHeatmap(showHeatmap.isSelected() ? panel.lastMoves : java.util.List.of());
                status.setText("Analysis complete: " + analysis.visits() + " visits. Navigate to refresh.");
                panel.positionSnapshot = controller.snapshot();
                panel.currentCandidates = analysis.moves().subList(0, Math.min(10, analysis.moves().size()));
                candidates.getItems().setAll(panel.currentCandidates.stream().map(move ->
                        String.format(Locale.ROOT, "%-5s  Black %5.1f%%  Visits %4d  %s",
                                move.move(), move.blackWinRate() * 100, move.visits(),
                                String.join(" ", move.variation()))).toList());
                result.setText(String.format(Locale.ROOT,
                        "Black win rate: %.1f%%    Black score lead: %+.2f",
                        analysis.blackWinRate() * 100, analysis.blackScoreLead()));
            }
        });
        start.setOnAction(e -> {
            if (controller.game() == null || !controller.game().type().equals(GameType.GO)) {
                status.setText("Go games only");
                return;
            }
            try {
                panel.adapter = panel.startEngine();
                panel.running = true;
                start.setDisable(true);
                stop.setDisable(false);
                panel.refresh(controller, status, result);
            } catch (Exception ex) {
                panel.stop();
                status.setText("KataGo could not start");
                result.setText(ex.toString());
            }
        });
        stop.setOnAction(e -> {
            panel.stop();
            start.setDisable(false);
            stop.setDisable(true);
            status.setText("Stopped");
        });
        GameController.Observer observer = (c, change) -> {
            if (change == GameController.Change.SAVED) return;
            if (panel.running) panel.refresh(c, status, result);
            else {
                panel.clearHeatmap();
                panel.clearPreview();
                status.setText("Position changed. Select Start Auto Analysis.");
                result.clear();
            }
        };
        controller.addObserver(observer);
        window.setOnHidden(e -> {
            controller.removeObserver(observer);
            panel.stop();
        });
        owner.addEventHandler(javafx.stage.WindowEvent.WINDOW_HIDDEN, e -> window.close());
        window.show();
    }

    private KataGoPanel() { }

    private KataGoAdapter startEngine() throws Exception {
        Path home = Path.of(System.getProperty("user.home"), "tools", "katago");
        return new KataGoAdapter(home.resolve("katago.exe"), home.resolve("analysis_example.cfg"),
                home.resolve("models").resolve("kata1-tf3-b10c512-s3203M-d5937M.bin.gz"),
                Path.of("C:/Program Files/NVIDIA/CUDNN/v9.28/bin/13.5/x64"));
    }

    private String describe(KataGoAnalysis result) {
        StringBuilder out = new StringBuilder();
        out.append(String.format(Locale.ROOT, "Black win rate: %.1f%%    Black score lead: %+.2f%n%n",
                result.blackWinRate() * 100, result.blackScoreLead()));
        out.append("Move     Black win%  Score lead  Visits  Variation\n");
        for (int i = 0; i < Math.min(10, result.moves().size()); i++) {
            var move = result.moves().get(i);
            out.append(String.format(Locale.ROOT, "%-8s %8.1f%% %10.2f %7d  %s%n",
                    move.move(), move.blackWinRate() * 100, move.blackScoreLead(),
                    move.visits(), String.join(" ", move.variation())));
        }
        return out.toString();
    }

    private void refresh(GameController controller, Label status, TextArea result) {
        clearHeatmap();
        clearPreview();
        status.setText("Analyzing selected position...");
        result.clear();
        latest.submit(() -> adapter.analyze(controller, 7.5, 500));
    }

    private void clearHeatmap() {
        lastMoves = java.util.List.of();
        board.setHeatmap(lastMoves);
    }

    private void clearPreview() {
        currentCandidates = java.util.List.of();
        positionSnapshot = null;
        if (candidatesList != null) candidatesList.getItems().clear();
    }

    private void stop() {
        running = false;
        clearHeatmap();
        clearPreview();
        latest.cancel();
        if (adapter != null) { adapter.close(); adapter = null; }
    }

    private KataGoAdapter adapter;
    private LatestAnalysis<KataGoAnalysis> latest;
    private BoardView board;
    private java.util.List<KataGoHeatmap.Spot> lastMoves = java.util.List.of();
    private ListView<String> candidatesList;
    private GameController.Snapshot positionSnapshot;
    private java.util.List<KataGoAnalysis.Candidate> currentCandidates = java.util.List.of();
    private boolean running;
}
