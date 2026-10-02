package com.tayek.mf;

import javafx.geometry.Point2D;

public interface BoardTransform {
    Point2D boardToScreen(double x, double y);
    Point2D screenToBoard(double x, double y);
}
