package com.tayek.mf;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public final class BoardView extends Canvas {
    private static final double horizontalSpacingMm=22.0,verticalSpacingMm=23.7,marginMm=14.0,stoneDiameterMm=23.0,hoverRadius=.38;
    private BoardPosition position;
    private final AudioClip stoneSound,atariSound;
    private FlatBoardTransform transform; private double scale; private int hoverX=-1,hoverY=-1; private Runnable positionChanged=()->{};
    public BoardView(BoardPosition position){this.position=position;stoneSound=loadSound("/audio/goclickb.wav");atariSound=loadSound("/audio/goatari.wav");widthProperty().addListener((o,a,b)->draw());heightProperty().addListener((o,a,b)->draw());setOnMouseMoved(e->updateHover(e.getX(),e.getY()));setOnMouseExited(e->clearHover());setOnMouseClicked(e->playHover());}
    private static AudioClip loadSound(String r){var u=BoardView.class.getResource(r);return u==null?null:new AudioClip(u.toExternalForm());}
    public void setPosition(BoardPosition p){position=p;refresh();}
    public BoardPosition position(){return position;}
    public void setOnPositionChanged(Runnable r){positionChanged=r==null?()->{}:r;}
    public void refresh(){hoverX=hoverY=-1;draw();positionChanged.run();}
    @Override public boolean isResizable(){return true;} @Override public double minWidth(double h){return 0;} @Override public double minHeight(double w){return 0;} @Override public double prefWidth(double h){return 760;} @Override public double prefHeight(double w){return 760;} @Override public double maxWidth(double h){return Double.MAX_VALUE;} @Override public double maxHeight(double w){return Double.MAX_VALUE;} @Override public void resize(double w,double h){setWidth(w);setHeight(h);}
    private void clearHover(){if(hoverX>=0){hoverX=hoverY=-1;draw();}}
    private void playHover(){if(hoverX>=0&&position.play(hoverX,hoverY)){if(stoneSound!=null)stoneSound.play();if(position.lastMoveCreatedAtari()&&atariSound!=null)atariSound.play();hoverX=hoverY=-1;draw();positionChanged.run();}}
    private void updateHover(double sx,double sy){if(transform==null)return;Point2D b=transform.screenToBoard(sx,sy);int x=(int)Math.round(b.getX()),y=(int)Math.round(b.getY()),nx=-1,ny=-1;if(x>=0&&x<position.width()&&y>=0&&y<position.height()){double dx=b.getX()-x,dy=b.getY()-y;if(Math.sqrt(dx*dx+dy*dy)<=hoverRadius&&position.stoneAt(x,y)==Stone.EMPTY){nx=x;ny=y;}}if(nx!=hoverX||ny!=hoverY){hoverX=nx;hoverY=ny;draw();}}
    private void draw(){double width=getWidth(),height=getHeight();if(width<=0||height<=0)return;int columns=position.width(),rows=position.height();double pw=2*marginMm+(columns-1)*horizontalSpacingMm,ph=2*marginMm+(rows-1)*verticalSpacingMm;scale=Math.min(width/pw,height/ph);double bw=pw*scale,bh=ph*scale,left=(width-bw)/2,top=(height-bh)/2;transform=new FlatBoardTransform(left+marginMm*scale,top+marginMm*scale,scale);GraphicsContext g=getGraphicsContext2D();g.clearRect(0,0,width,height);g.setFill(Color.rgb(218,174,92));g.fillRect(left,top,bw,bh);g.setStroke(Color.rgb(45,36,22));g.setLineWidth(Math.max(1,scale));for(int x=0;x<columns;x++){Point2D a=transform.boardToScreen(x,0),b=transform.boardToScreen(x,rows-1);g.strokeLine(a.getX(),a.getY(),b.getX(),b.getY());}for(int y=0;y<rows;y++){Point2D a=transform.boardToScreen(0,y),b=transform.boardToScreen(columns-1,y);g.strokeLine(a.getX(),a.getY(),b.getX(),b.getY());}drawCoordinates(g);if(columns==19&&rows==19)drawHoshi(g);for(int y=0;y<rows;y++)for(int x=0;x<columns;x++)if(position.stoneAt(x,y)!=Stone.EMPTY)drawStone(g,x,y,position.stoneAt(x,y));drawLastMove(g);if(hoverX>=0)drawHoverStone(g,hoverX,hoverY,position.sideToMove());}
    private void drawLastMove(GraphicsContext g){Move m=position.lastMove();if(m==null||m.x()<0||m.y()<0)return;Point2D p=transform.boardToScreen(m.x(),m.y());double d=5*scale;g.setFill(m.stone()==Stone.BLACK?Color.WHITE:Color.BLACK);g.fillOval(p.getX()-d/2,p.getY()-d/2,d,d);}
    private void drawCoordinates(GraphicsContext g){g.setFont(Font.font("Serif",Math.max(10,7.5*scale)));g.setFill(Color.rgb(45,36,22));g.setTextAlign(TextAlignment.CENTER);g.setTextBaseline(VPos.CENTER);double xo=9*scale,yo=9*scale;for(int x=0;x<position.width();x++){Point2D t=transform.boardToScreen(x,0),b=transform.boardToScreen(x,position.height()-1);String s=columnLabel(x);g.fillText(s,t.getX(),t.getY()-yo);g.fillText(s,b.getX(),b.getY()+yo);}for(int y=0;y<position.height();y++){Point2D l=transform.boardToScreen(0,y),r=transform.boardToScreen(position.width()-1,y);String s=Integer.toString(position.height()-y);g.fillText(s,l.getX()-xo,l.getY());g.fillText(s,r.getX()+xo,r.getY());}}
    private static String columnLabel(int x){int c='A'+x;if(c>='I')c++;return Character.toString((char)c);}
    private void drawHoshi(GraphicsContext g){int[]ps={3,9,15};double d=4*scale;g.setFill(Color.rgb(45,36,22));for(int x:ps)for(int y:ps){Point2D p=transform.boardToScreen(x,y);g.fillOval(p.getX()-d/2,p.getY()-d/2,d,d);}}
    private void drawHoverStone(GraphicsContext g,int x,int y,Stone s){Point2D p=transform.boardToScreen(x,y);double d=stoneDiameterMm*scale,r=d/2;if(s==Stone.BLACK){g.setFill(Color.rgb(10,10,10,.68));g.setStroke(Color.rgb(0,0,0,.78));}else{g.setFill(Color.rgb(245,245,238,.78));g.setStroke(Color.rgb(95,95,90,.78));}g.fillOval(p.getX()-r,p.getY()-r,d,d);g.setLineWidth(Math.max(.7,.45*scale));g.strokeOval(p.getX()-r,p.getY()-r,d,d);}
    private void drawStone(GraphicsContext g,int x,int y,Stone s){Point2D p=transform.boardToScreen(x,y);double d=stoneDiameterMm*scale,r=d/2,so=1.2*scale;g.setFill(Color.rgb(0,0,0,.22));g.fillOval(p.getX()-r+so,p.getY()-r+so,d,d);RadialGradient gr;if(s==Stone.BLACK)gr=new RadialGradient(0,0,p.getX()-r*.35,p.getY()-r*.38,r*1.35,false,CycleMethod.NO_CYCLE,new Stop(0,Color.rgb(105,105,105)),new Stop(.28,Color.rgb(45,45,45)),new Stop(1,Color.rgb(3,3,3)));else gr=new RadialGradient(0,0,p.getX()-r*.35,p.getY()-r*.38,r*1.35,false,CycleMethod.NO_CYCLE,new Stop(0,Color.WHITE),new Stop(.62,Color.rgb(245,245,238)),new Stop(1,Color.rgb(178,178,170)));g.setFill(gr);g.fillOval(p.getX()-r,p.getY()-r,d,d);g.setStroke(s==Stone.BLACK?Color.rgb(0,0,0,.85):Color.rgb(95,95,90,.85));g.setLineWidth(Math.max(.7,.45*scale));g.strokeOval(p.getX()-r,p.getY()-r,d,d);}
}
