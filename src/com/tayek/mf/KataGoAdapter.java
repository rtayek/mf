package com.tayek.mf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Owns a single KataGo analysis process. No game nodes or JavaFX state are shared. */
public final class KataGoAdapter implements AutoCloseable {
    public KataGoAdapter(Path executable, Path config, Path model, Path cudnnDirectory) throws IOException {
        List<String> command = List.of(executable.toAbsolutePath().toString(), "analysis", "-config",
                config.toAbsolutePath().toString(), "-model", model.toAbsolutePath().toString());
        ProcessBuilder builder = new ProcessBuilder(command).directory(executable.toAbsolutePath().getParent().toFile());
        if (cudnnDirectory != null) {
            Map<String, String> env = builder.environment();
            env.put("PATH", "C:\\Windows\\System32;C:\\Windows;"
                    + cudnnDirectory.toAbsolutePath() + ";"
                    + "C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v13.2\\bin\\x64;"
                    + env.getOrDefault("PATH", ""));
        }
        process = builder.start();
        input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        Thread errors = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    synchronized (diagnostics) {
                        if (diagnostics.length() > 8192) diagnostics.delete(0, 4096);
                        diagnostics.append(line).append('\n');
                    }
                }
            } catch (IOException ignored) { }
        }, "katago-stderr");
        errors.setDaemon(true);
        errors.start();
        Thread reader = new Thread(this::readResponses, "katago-responses");
        reader.setDaemon(true);
        reader.start();
    }

    /** Analyze a copied board state. This intentionally does not invent a move history. */
    public CompletableFuture<KataGoAnalysis> analyze(BoardPosition board, double komi, int maxVisits) {
        Objects.requireNonNull(board);
        if (maxVisits < 1) throw new IllegalArgumentException("maxVisits must be positive");
        String id = UUID.randomUUID().toString();
        String query = query(id, board, komi, maxVisits);
        CompletableFuture<KataGoAnalysis> future = new CompletableFuture<>();
        synchronized (input) {
            if (!process.isAlive()) {
                future.completeExceptionally(new IOException("KataGo stopped: " + diagnostics()));
                return future;
            }
            pending.put(id, future);
            try { input.write(query); input.newLine(); input.flush(); }
            catch (IOException ex) { pending.remove(id); future.completeExceptionally(ex); }
        }
        return future;
    }

    static String query(String id, BoardPosition board, double komi, int maxVisits) {
        StringBuilder json = new StringBuilder("{\"id\":\"").append(id).append("\",\"initialStones\":[");
        boolean first = true;
        for (int y = 0; y < board.height(); y++) for (int x = 0; x < board.width(); x++) {
            Stone stone = board.stoneAt(x, y);
            if (stone == Stone.EMPTY) continue;
            if (!first) json.append(',');
            first = false;
            json.append("[\"").append(stone == Stone.BLACK ? 'B' : 'W').append("\",\"")
                    .append(coordinate(x, y, board.height())).append("\"]");
        }
        String player = board.sideToMove() == Stone.WHITE ? "W" : "B";
        return json.append("],\"moves\":[],\"initialPlayer\":\"").append(player)
                .append("\",\"rules\":\"chinese\",\"komi\":").append(komi)
                .append(",\"boardXSize\":").append(board.width())
                .append(",\"boardYSize\":").append(board.height())
                .append(",\"maxVisits\":").append(maxVisits).append('}').toString();
    }

    private static String coordinate(int x, int y, int height) {
        int column = x >= 8 ? x + 1 : x;
        return "" + (char) ('A' + column) + (height - y);
    }

    private void readResponses() {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = output.readLine()) != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) KataGoJson.parse(line);
                    String id = (String) message.get("id");
                    if (id == null) continue;
                    if (message.containsKey("warning")) continue;
                    CompletableFuture<KataGoAnalysis> future = pending.remove(id);
                    if (future == null) continue;
                    if (message.containsKey("error")) future.completeExceptionally(
                            new IOException(String.valueOf(message.get("error"))));
                    else if (Boolean.FALSE.equals(message.get("isDuringSearch")))
                        future.complete(KataGoAnalysis.fromJson(line));
                    else pending.put(id, future);
                } catch (RuntimeException ex) { failAll(ex); }
            }
            failAll(new IOException("KataGo exited: " + diagnostics()));
        } catch (IOException ex) { failAll(ex); }
    }

    private void failAll(Throwable error) {
        for (var entry : new ArrayList<>(pending.entrySet())) {
            if (pending.remove(entry.getKey(), entry.getValue())) entry.getValue().completeExceptionally(error);
        }
    }

    public String diagnostics() { synchronized (diagnostics) { return diagnostics.toString(); } }

    @Override public void close() {
        failAll(new IOException("KataGo stopped"));
        process.destroy();
        try { input.close(); } catch (IOException ignored) { }
        if (process.isAlive()) process.destroyForcibly();
    }

    private final Process process;
    private final BufferedWriter input;
    private final StringBuilder diagnostics = new StringBuilder();
    private final Map<String, CompletableFuture<KataGoAnalysis>> pending = new ConcurrentHashMap<>();
}
