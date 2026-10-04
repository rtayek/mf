package com.tayek.mf;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Deliberately small renderer for the Gomoku design probe. */
public final class GomokuView extends Canvas {
    private GomokuPosition position;

    public GomokuView(GomokuPosition position) {
        this.position = position;
        widthProperty().addListener((o,a,b) -> draw());
        heightProperty().addListener((o,a,b) -> draw());
    }

    public void setPosition(GomokuPosition position) { this.position = position; draw(); }
    @Override public boolean isResizable(){return true;}
    @Override public double minWidth(double h){return 0;}
    @Override public double minHeight(double w){return 0;}
    @Override public double prefWidth(double h){return 760;}
    @Override public double prefHeight(double w){return 760;}
    @Override public double maxWidth(double h){return Double.MAX_VALUE;}
    @Override public double maxHeight(double w){return Double.MAX_VALUE;}
    @Override public void resize(double w,double h){setWidth(w);setHeight(h);draw();}

    private void draw() {
        double width=getWidth(), height=getHeight();
        if(width<=0||height<=0)return;
        GraphicsContext g=getGraphicsContext2D();
        g.clearRect(0,0,width,height);
        int columns=position.width(), rows=position.height();
        double margin=30;
        double spacing=Math.min((width-2*margin)/Math.max(1,columns-1),(height-2*margin)/Math.max(1,rows-1));
        double boardWidth=(columns-1)*spacing, boardHeight=(rows-1)*spacing;
        double left=(width-boardWidth)/2, top=(height-boardHeight)/2;
        g.setFill(Color.rgb(218,174,92));
        g.fillRect(left-margin/2,top-margin/2,boardWidth+margin,boardHeight+margin);
        g.setStroke(Color.rgb(45,36,22));
        for(int x=0;x<columns;x++)g.strokeLine(left+x*spacing,top,left+x*spacing,top+boardHeight);
        for(int y=0;y<rows;y++)g.strokeLine(left,top+y*spacing,left+boardWidth,top+y*spacing);
        double d=spacing*.88;
        for(int y=0;y<rows;y++)for(int x=0;x<columns;x++){
            Stone stone=position.stoneAt(x,y);
            if(stone==Stone.EMPTY)continue;
            double cx=left+x*spacing,cy=top+y*spacing;
            g.setFill(stone==Stone.BLACK?Color.BLACK:Color.WHITE);
            g.fillOval(cx-d/2,cy-d/2,d,d);
            g.setStroke(Color.rgb(50,50,50));
            g.strokeOval(cx-d/2,cy-d/2,d,d);
        }
    }
}
