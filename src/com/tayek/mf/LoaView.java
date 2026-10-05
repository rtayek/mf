package com.tayek.mf;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Simple square-board renderer for the Lines of Action replay probe. */
public final class LoaView extends Canvas {
    private LoaPosition position;

    public LoaView(LoaPosition position) {
        this.position = position;
        widthProperty().addListener((o,a,b) -> draw());
        heightProperty().addListener((o,a,b) -> draw());
    }

    public void setPosition(LoaPosition position) { this.position = position; draw(); }
    @Override public boolean isResizable() { return true; }
    @Override public double minWidth(double h) { return 0; }
    @Override public double minHeight(double w) { return 0; }
    @Override public double prefWidth(double h) { return 760; }
    @Override public double prefHeight(double w) { return 760; }
    @Override public double maxWidth(double h) { return Double.MAX_VALUE; }
    @Override public double maxHeight(double w) { return Double.MAX_VALUE; }
    @Override public void resize(double w, double h) { setWidth(w); setHeight(h); draw(); }

    private void draw() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        double margin = 34;
        double cell = Math.min((w - 2 * margin) / 8.0, (h - 2 * margin) / 8.0);
        double left = (w - 8 * cell) / 2.0, top = (h - 8 * cell) / 2.0;
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) {
            double px = left + x * cell, py = top + y * cell;
            g.setFill(((x + y) & 1) == 0 ? Color.rgb(232, 210, 164) : Color.rgb(177, 132, 83));
            g.fillRect(px, py, cell, cell);
            Stone stone = position.stoneAt(x, y);
            if (stone != Stone.EMPTY) {
                double d = cell * .72;
                g.setFill(stone == Stone.BLACK ? Color.BLACK : Color.WHITE);
                g.fillOval(px + (cell-d)/2, py + (cell-d)/2, d, d);
                g.setStroke(Color.rgb(55,55,55));
                g.strokeOval(px + (cell-d)/2, py + (cell-d)/2, d, d);
            }
        }
        g.setFill(Color.BLACK);
        for (int x = 0; x < 8; x++) g.fillText(Character.toString((char)('A'+x)), left + (x+.45)*cell, top + 8*cell + 18);
        for (int y = 0; y < 8; y++) g.fillText(Integer.toString(8-y), left - 20, top + (y+.58)*cell);
    }
}
