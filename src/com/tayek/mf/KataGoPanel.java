package com.tayek.mf;

import java.nio.file.Path;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Separate, read-only analysis panel for an independent GameController snapshot. */
final class KataGoPanel {
    static void open(GameController controller, Stage owner) {
        KataGoPanel panel = new KataGoPanel();
        Stage window = new Stage();
        window.initOwner(owner);
        window.setTitle("MF - KataGo Analysis");
        Button start = new Button("Start / Analyze Position");
        Button stop = new Button("Stop");
        Label status = new Label("Select a Go position, then start analysis");
        TextArea result = new TextArea();
        result.setEditable(false);
        result.setStyle("-fx-font-family: monospace; -fx-font-size: 16px;");
        VBox layout = new VBox(12, new HBox(12, start, stop), status, result);
        layout.setPadding(new Insets(12));
        VBox.setVgrow(result, Priority.ALWAYS);
        window.setScene(new Scene(layout, 630, 440));
        start.setOnAction(e -> {
            if (controller.game() == null || !controller.game().type().equals(GameType.GO)) {
                status.setText("Go games only");
                return;
            }
            try {
                if (panel.adapter == null) panel.adapter = panel.startEngine();
                BoardPosition position = controller.position();
                long generation = ++panel.generation;
                status.setText("Analyzing selected position...");
                result.clear();
                panel.adapter.analyze(position, 7.5, 100).whenComplete((analysis, error) -> Platform.runLater(() -> {
                    if (generation != panel.generation) return;
                    if (error != null) {
                        status.setText("Analysis failed");
                        result.setText(error.getCause() == null ? error.toString() : error.getCause().toString());
                    } else {
                        status.setText("Analysis complete: " + analysis.visits() + " visits");
                        result.setText(panel.describe(analysis));
                    }
                }));
            } catch (Exception ex) {
                status.setText("KataGo could not start");
                result.setText(ex.toString());
            }
        });
        stop.setOnAction(e -> {
            panel.stop();
            status.setText("Stopped");
        });
        GameController.Observer observer = (c, change) -> {
            panel.generation++;
            status.setText("Position changed. Select Start to analyze.");
            result.clear();
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

    private void stop() {
        generation++;
        if (adapter != null) { adapter.close(); adapter = null; }
    }

    private KataGoAdapter adapter;
    private long generation;
}
