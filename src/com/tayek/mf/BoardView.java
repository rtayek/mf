package com.tayek.mf;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class BoardView extends Canvas {
    private static final double horizontalSpacingMm = 22.0;
    private static final double verticalSpacingMm = 23.7;
    private static final double marginMm = 14.0;

    private final int columns;
    private final int rows;

    public BoardView(int columns, int rows) {
        if (columns < 2 || rows < 2) {
            throw new IllegalArgumentException("board must be at least 2 x 2");
        }
        this.columns = columns;
        this.rows = rows;

        widthProperty().addListener((observable, oldValue, newValue) -> draw());
        heightProperty().addListener((observable, oldValue, newValue) -> draw());
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

    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        double physicalWidth = 2 * marginMm + (columns - 1) * horizontalSpacingMm;
        double physicalHeight = 2 * marginMm + (rows - 1) * verticalSpacingMm;
        double scale = Math.min(width / physicalWidth, height / physicalHeight);

        double boardWidth = physicalWidth * scale;
        double boardHeight = physicalHeight * scale;
        double left = (width - boardWidth) / 2;
        double top = (height - boardHeight) / 2;
        double originX = left + marginMm * scale;
        double originY = top + marginMm * scale;

        FlatBoardTransform transform = new FlatBoardTransform(originX, originY, scale);
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
}
