package com.tayek.mf;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public final class BoardView extends Canvas {
    private static final double horizontalSpacingMm = 22.0;
    private static final double verticalSpacingMm = 23.7;
    private static final double marginMm = 14.0;
    private static final double stoneDiameterMm = 23.0;
    private static final double hoverRadius = 0.38;

    private final BoardPosition position;
    private final AudioClip stoneSound;
    private FlatBoardTransform transform;
    private double scale;
    private int hoverX = -1;
    private int hoverY = -1;
    private Runnable positionChanged = () -> { };

    public BoardView(BoardPosition position) {
        this.position = position;
        var soundUrl = BoardView.class.getResource("/audio/goclickb.wav");
        stoneSound = soundUrl == null ? null : new AudioClip(soundUrl.toExternalForm());
        widthProperty().addListener((observable, oldValue, newValue) -> draw());
        heightProperty().addListener((observable, oldValue, newValue) -> draw());
        setOnMouseMoved(event -> updateHover(event.getX(), event.getY()));
        setOnMouseExited(event -> clearHover());
        setOnMouseClicked(event -> playHover());
    }

    public void setOnPositionChanged(Runnable positionChanged) {
        this.positionChanged = positionChanged == null ? () -> { } : positionChanged;
    }

    public void refresh() {
        hoverX = -1;
        hoverY = -1;
        draw();
        positionChanged.run();
    }

    @Override public boolean isResizable() { return true; }
    @Override public double prefWidth(double height) { return 760; }
    @Override public double prefHeight(double width) { return 820; }

    private void clearHover() {
        if (hoverX >= 0) {
            hoverX = -1;
            hoverY = -1;
            draw();
        }
    }

    private void playHover() {
        if (hoverX >= 0 && position.play(hoverX, hoverY)) {
            if (stoneSound != null) stoneSound.play();
            hoverX = -1;
            hoverY = -1;
            draw();
            positionChanged.run();
        }
    }

    private void updateHover(double screenX, double screenY) {
        if (transform == null) return;
        Point2D board = transform.screenToBoard(screenX, screenY);
        int x = (int) Math.round(board.getX());
        int y = (int) Math.round(board.getY());
        int newX = -1;
        int newY = -1;
        if (x >= 0 && x < position.width() && y >= 0 && y < position.height()) {
            double dx = board.getX() - x;
            double dy = board.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) <= hoverRadius && position.stoneAt(x, y) == Stone.EMPTY) {
                newX = x;
                newY = y;
            }
        }
        if (newX != hoverX || newY != hoverY) {
            hoverX = newX;
            hoverY = newY;
            draw();
        }
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) return;
        int columns = position.width();
        int rows = position.height();
        double physicalWidth = 2 * marginMm + (columns - 1) * horizontalSpacingMm;
        double physicalHeight = 2 * marginMm + (rows - 1) * verticalSpacingMm;
        scale = Math.min(width / physicalWidth, height / physicalHeight);
        double boardWidth = physicalWidth * scale;
        double boardHeight = physicalHeight * scale;
        double left = (width - boardWidth) / 2;
        double top = (height - boardHeight) / 2;
        transform = new FlatBoardTransform(left + marginMm * scale, top + marginMm * scale, scale);

        GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, width, height);
        g.setFill(Color.rgb(218, 174, 92));
        g.fillRect(left, top, boardWidth, boardHeight);
        g.setStroke(Color.rgb(45, 36, 22));
        g.setLineWidth(Math.max(1.0, scale));
        for (int x = 0; x < columns; x++) {
            Point2D a = transform.boardToScreen(x, 0), b = transform.boardToScreen(x, rows - 1);
            g.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
        for (int y = 0; y < rows; y++) {
            Point2D a = transform.boardToScreen(0, y), b = transform.boardToScreen(columns - 1, y);
            g.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
        drawCoordinates(g, transform, scale);
        if (columns == 19 && rows == 19) drawHoshi(g, transform, scale);
        for (int y = 0; y < rows; y++) for (int x = 0; x < columns; x++) {
            Stone stone = position.stoneAt(x, y);
            if (stone != Stone.EMPTY) drawStone(g, transform, scale, x, y, stone);
        }
        drawLastMove(g);
        if (hoverX >= 0) drawHoverStone(g, transform, scale, hoverX, hoverY, position.sideToMove());
    }

    private void drawLastMove(GraphicsContext g) {
        Move move = position.lastMove();
        if (move == null) return;
        Point2D p = transform.boardToScreen(move.x(), move.y());
        double diameter = 5.0 * scale;
        g.setFill(move.stone() == Stone.BLACK ? Color.WHITE : Color.BLACK);
        g.fillOval(p.getX() - diameter / 2, p.getY() - diameter / 2, diameter, diameter);
    }

    private void drawCoordinates(GraphicsContext g, BoardTransform transform, double scale) {
        g.setFont(Font.font("Serif", Math.max(10.0, 7.5 * scale)));
        g.setFill(Color.rgb(45, 36, 22));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        double xOffset = 9.0 * scale, yOffset = 9.0 * scale;
        for (int x = 0; x < position.width(); x++) {
            Point2D top = transform.boardToScreen(x, 0), bottom = transform.boardToScreen(x, position.height() - 1);
            String label = columnLabel(x);
            g.fillText(label, top.getX(), top.getY() - yOffset);
            g.fillText(label, bottom.getX(), bottom.getY() + yOffset);
        }
        for (int y = 0; y < position.height(); y++) {
            Point2D left = transform.boardToScreen(0, y), right = transform.boardToScreen(position.width() - 1, y);
            String label = Integer.toString(position.height() - y);
            g.fillText(label, left.getX() - xOffset, left.getY());
            g.fillText(label, right.getX() + xOffset, right.getY());
        }
    }

    private static String columnLabel(int x) {
        int letter = 'A' + x;
        if (letter >= 'I') letter++;
        return Character.toString((char) letter);
    }

    private static void drawHoshi(GraphicsContext g, BoardTransform transform, double scale) {
        int[] points = {3, 9, 15};
        double diameter = 4.0 * scale;
        g.setFill(Color.rgb(45, 36, 22));
        for (int x : points) for (int y : points) {
            Point2D p = transform.boardToScreen(x, y);
            g.fillOval(p.getX() - diameter / 2, p.getY() - diameter / 2, diameter, diameter);
        }
    }

    private static void drawHoverStone(GraphicsContext g, BoardTransform transform, double scale, int x, int y, Stone stone) {
        Point2D p = transform.boardToScreen(x, y);
        double diameter = stoneDiameterMm * scale, radius = diameter / 2;
        if (stone == Stone.BLACK) {
            g.setFill(Color.rgb(10, 10, 10, 0.68)); g.setStroke(Color.rgb(0, 0, 0, 0.78));
        } else {
            g.setFill(Color.rgb(245, 245, 238, 0.78)); g.setStroke(Color.rgb(95, 95, 90, 0.78));
        }
        g.fillOval(p.getX() - radius, p.getY() - radius, diameter, diameter);
        g.setLineWidth(Math.max(0.7, 0.45 * scale));
        g.strokeOval(p.getX() - radius, p.getY() - radius, diameter, diameter);
    }

    private static void drawStone(GraphicsContext g, BoardTransform transform, double scale, int x, int y, Stone stone) {
        Point2D p = transform.boardToScreen(x, y);
        double diameter = stoneDiameterMm * scale, radius = diameter / 2;
        g.setFill(Color.rgb(0, 0, 0, 0.22));
        double shadowOffset = 1.2 * scale;
        g.fillOval(p.getX() - radius + shadowOffset, p.getY() - radius + shadowOffset, diameter, diameter);
        RadialGradient gradient;
        if (stone == Stone.BLACK) {
            gradient = new RadialGradient(0, 0, p.getX() - radius * 0.35, p.getY() - radius * 0.38,
                    radius * 1.35, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.rgb(105, 105, 105)), new Stop(0.28, Color.rgb(45, 45, 45)), new Stop(1.0, Color.rgb(3, 3, 3)));
        } else {
            gradient = new RadialGradient(0, 0, p.getX() - radius * 0.35, p.getY() - radius * 0.38,
                    radius * 1.35, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.WHITE), new Stop(0.62, Color.rgb(245, 245, 238)), new Stop(1.0, Color.rgb(178, 178, 170)));
        }
        g.setFill(gradient);
        g.fillOval(p.getX() - radius, p.getY() - radius, diameter, diameter);
        g.setStroke(stone == Stone.BLACK ? Color.rgb(0, 0, 0, 0.85) : Color.rgb(95, 95, 90, 0.85));
        g.setLineWidth(Math.max(0.7, 0.45 * scale));
        g.strokeOval(p.getX() - radius, p.getY() - radius, diameter, diameter);
    }
}
