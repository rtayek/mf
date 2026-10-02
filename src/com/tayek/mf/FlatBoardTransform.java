package com.tayek.mf;

import javafx.geometry.Point2D;

public final class FlatBoardTransform implements BoardTransform {
    public static final double horizontalSpacingMm = 22.0;
    public static final double verticalSpacingMm = 23.7;
    public static final double stoneDiameterMm = 22.5;

    private final double originX;
    private final double originY;
    private final double dx;
    private final double dy;

    public FlatBoardTransform(double originX, double originY, double scale) {
        this.originX = originX;
        this.originY = originY;
        dx = horizontalSpacingMm * scale;
        dy = verticalSpacingMm * scale;
    }

    @Override
    public Point2D boardToScreen(double x, double y) {
        return new Point2D(originX + x * dx, originY + y * dy);
    }

    @Override
    public Point2D screenToBoard(double x, double y) {
        return new Point2D((x - originX) / dx, (y - originY) / dy);
    }

    public double dx() {
        return dx;
    }

    public double dy() {
        return dy;
    }
}
