package com.tayek.mf;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

public final class BoardView extends Canvas {
    private static final double horizontalSpacingMm = 22.0;
    private static final double verticalSpacingMm = 23.7;
    private static final double marginMm = 14.0;
    private static final double stoneDiameterMm = 23.0;
    private static final double hoverRadius = 0.38;

    private final int columns;
    private final int rows;
    private FlatBoardTransform transform;
    private double scale;
    private int hoverX = -1;
    private int hoverY = -1;

    public BoardView(int columns, int rows) {
        if (columns < 2 || rows < 2) {
            throw new IllegalArgumentException("board must be at least 2 x 2");
        }
        this.columns = columns;
        this.rows = rows;

        widthProperty().addListener((observable, oldValue, newValue) -> draw());
        heightProperty().addListener((observable, oldValue, newValue) -> draw());

        setOnMouseMoved(event -> updateHover(event.getX(), event.getY()));
        setOnMouseExited(event -> {
            if (hoverX >= 0) {
                hoverX = -1;
                hoverY = -1;
                draw();
            }
        });
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return 760;
    }

    @Override
    public double prefHeight(double width) {
        return 820;
    }

    private void updateHover(double screenX, double screenY) {
        if (transform == null) {
            return;
        }

        Point2D board = transform.screenToBoard(screenX, screenY);
        int x = (int) Math.round(board.getX());
        int y = (int) Math.round(board.getY());
        int newX = -1;
        int newY = -1;

        if (x >= 0 && x < columns && y >= 0 && y < rows) {
            double dx = board.getX() - x;
            double dy = board.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= hoverRadius && !hasSampleStone(x, y)) {
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
        if (width <= 0 || height <= 0) {
            return;
        }

        double physicalWidth = 2 * marginMm + (columns - 1) * horizontalSpacingMm;
        double physicalHeight = 2 * marginMm + (rows - 1) * verticalSpacingMm;
        scale = Math.min(width / physicalWidth, height / physicalHeight);

        double boardWidth = physicalWidth * scale;
        double boardHeight = physicalHeight * scale;
        double left = (width - boardWidth) / 2;
        double top = (height - boardHeight) / 2;
        double originX = left + marginMm * scale;
        double originY = top + marginMm * scale;

        transform = new FlatBoardTransform(originX, originY, scale);
        GraphicsContext g = getGraphicsContext2D();

        g.clearRect(0, 0, width, height);
        g.setFill(Color.rgb(218, 174, 92));
        g.fillRect(left, top, boardWidth, boardHeight);

        g.setStroke(Color.rgb(45, 36, 22));
        g.setLineWidth(Math.max(1.0, scale));

        for (int x = 0; x < columns; x++) {
            Point2D a = transform.boardToScreen(x, 0);
            Point2D b = transform.boardToScreen(x, rows - 1);
            g.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
        for (int y = 0; y < rows; y++) {
            Point2D a = transform.boardToScreen(0, y);
            Point2D b = transform.boardToScreen(columns - 1, y);
            g.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }

        if (columns == 19 && rows == 19) {
            drawHoshi(g, transform, scale);
            drawSampleStones(g, transform, scale);
        }

        if (hoverX >= 0) {
            drawHoverStone(g, transform, scale, hoverX, hoverY);
        }
    }

    private static void drawHoshi(GraphicsContext g, BoardTransform transform, double scale) {
        int[] points = {3, 9, 15};
        double diameter = 4.0 * scale;
        g.setFill(Color.rgb(45, 36, 22));
        for (int x : points) {
            for (int y : points) {
                Point2D p = transform.boardToScreen(x, y);
                g.fillOval(p.getX() - diameter / 2, p.getY() - diameter / 2, diameter, diameter);
            }
        }
    }

    private static void drawSampleStones(GraphicsContext g, BoardTransform transform, double scale) {
        drawStone(g, transform, scale, 3, 3, true);
        drawStone(g, transform, scale, 4, 3, false);
        drawStone(g, transform, scale, 9, 9, true);
        drawStone(g, transform, scale, 9, 10, false);
        drawStone(g, transform, scale, 14, 15, true);
        drawStone(g, transform, scale, 15, 15, false);
    }

    private static boolean hasSampleStone(int x, int y) {
        return (x == 3 && y == 3) || (x == 4 && y == 3)
                || (x == 9 && y == 9) || (x == 9 && y == 10)
                || (x == 14 && y == 15) || (x == 15 && y == 15);
    }

    private static void drawHoverStone(GraphicsContext g, BoardTransform transform, double scale,
            int x, int y) {
        Point2D p = transform.boardToScreen(x, y);
        double diameter = stoneDiameterMm * scale;
        double radius = diameter / 2;
        g.setFill(Color.rgb(20, 20, 20, 0.48));
        g.fillOval(p.getX() - radius, p.getY() - radius, diameter, diameter);
        g.setStroke(Color.rgb(0, 0, 0, 0.58));
        g.setLineWidth(Math.max(0.7, 0.45 * scale));
        g.strokeOval(p.getX() - radius, p.getY() - radius, diameter, diameter);
    }

    private static void drawStone(GraphicsContext g, BoardTransform transform, double scale,
            int x, int y, boolean black) {
        Point2D p = transform.boardToScreen(x, y);
        double diameter = stoneDiameterMm * scale;
        double radius = diameter / 2;

        g.setFill(Color.rgb(0, 0, 0, 0.22));
        double shadowOffset = 1.2 * scale;
        g.fillOval(p.getX() - radius + shadowOffset, p.getY() - radius + shadowOffset,
                diameter, diameter);

        RadialGradient gradient;
        if (black) {
            gradient = new RadialGradient(0, 0,
                    p.getX() - radius * 0.35, p.getY() - radius * 0.38,
                    radius * 1.35, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.rgb(105, 105, 105)),
                    new Stop(0.28, Color.rgb(45, 45, 45)),
                    new Stop(1.0, Color.rgb(3, 3, 3)));
        } else {
            gradient = new RadialGradient(0, 0,
                    p.getX() - radius * 0.35, p.getY() - radius * 0.38,
                    radius * 1.35, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.WHITE),
                    new Stop(0.62, Color.rgb(245, 245, 238)),
                    new Stop(1.0, Color.rgb(178, 178, 170)));
        }

        g.setFill(gradient);
        g.fillOval(p.getX() - radius, p.getY() - radius, diameter, diameter);

        g.setStroke(black ? Color.rgb(0, 0, 0, 0.85) : Color.rgb(95, 95, 90, 0.85));
        g.setLineWidth(Math.max(0.7, 0.45 * scale));
        g.strokeOval(p.getX() - radius, p.getY() - radius, diameter, diameter);
    }
}
