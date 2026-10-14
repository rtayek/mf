package com.tayek.mf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Locale;

/** Small interactive and scriptable text view of a shared game controller. */
public final class MfCli {
    private final GameController controller;
    private final PrintStream out;
    public MfCli(GameController controller, PrintStream out) {
        this.controller = controller;
        this.out = out;
        controller.addObserver((c, change) -> printBoard());
    }
    public void printBoard() {
        if (controller.game() == null) { out.println("No game open."); return; }
        if (controller.game().type() != GameType.GO) {
            out.println("Text board is currently available for Go only."); return;
        }
        BoardPosition board = controller.position();
        int size = board.width();
        for (int y = 0; y < size; y++) {
            out.printf("%2d ", size - y);
            for (int x = 0; x < size; x++) {
                Stone stone = board.stoneAt(x, y);
                out.print(stone == Stone.BLACK ? "X " : stone == Stone.WHITE ? "O " : ". ");
            }
            out.println();
        }
        out.print("   ");
        for (int x = 0; x < size; x++) out.print((char) ('A' + x + (x >= 8 ? 1 : 0)) + " ");
        out.println();
        out.println("Move " + board.moveNumber() + " | Next " + board.sideToMove()
                + " | Children " + controller.current().children().size()
                + (controller.modified() ? " | modified" : ""));
    }
    /** Returns false on quit. */
    public boolean execute(String line) throws IOException {
        String[] words = line.trim().split("\\s+");
        if (words[0].isEmpty()) return true;
        String command = words[0].toLowerCase(Locale.ROOT);
        boolean ok;
        try {
            switch (command) {
                case "quit", "q", "exit" -> { return false; }
                case "help", "h" -> {
                    out.println("open FILE | save [FILE] | board | left right up down | move X Y | pass | delete | undo redo | quit");
                    return true;
                }
                case "open" -> { controller.open(Path.of(argument(line))); return true; }
                case "save" -> {
                    if (words.length > 1) controller.save(Path.of(argument(line)));
                    else controller.save();
                    return true;
                }
                case "board" -> { printBoard(); return true; }
                case "left" -> ok = controller.left();
                case "right" -> ok = controller.right();
                case "up" -> ok = controller.up();
                case "down" -> ok = controller.down();
                case "delete" -> ok = controller.delete();
                case "undo" -> ok = controller.undo();
                case "redo" -> ok = controller.redo();
                case "pass" -> ok = controller.pass();
                case "resign" -> ok = controller.resign();
                case "move" -> {
                    if (words.length != 3) throw new IllegalArgumentException("usage: move X Y (zero-based SGF coordinates)");
                    ok = controller.move(Integer.parseInt(words[1]), Integer.parseInt(words[2]));
                }
                default -> { out.println("Unknown command: " + command); return true; }
            }
            if (!ok) out.println("No change.");
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException e) {
            out.println("Error: " + e.getMessage());
        }
        return true;
    }
    private String argument(String line) {
        int i = line.indexOf(' ');
        if (i < 0 || line.substring(i + 1).isBlank()) throw new IllegalArgumentException("missing file path");
        return line.substring(i + 1).trim();
    }
    public static void main(String[] args) throws IOException {
        GameController controller = new GameController();
        MfCli cli = new MfCli(controller, System.out);
        if (args.length > 0) controller.open(Path.of(args[0]));
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = input.readLine()) != null) {
            if (!cli.execute(line)) break;
        }
    }
}
