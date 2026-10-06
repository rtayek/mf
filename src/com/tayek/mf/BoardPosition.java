package com.tayek.mf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BoardPosition {
    private static final int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
    private final int width, height;
    private final Stone[][] stones;
    private final List<Move> moves = new ArrayList<>();
    private int moveNumber;
    private Stone sideToMoveOverride;
    private boolean lastMoveCreatedAtari;

    public BoardPosition(int width, int height) {
        if (width < 2 || height < 2) throw new IllegalArgumentException("board must be at least 2 x 2");
        this.width=width; this.height=height; stones=new Stone[height][width]; rebuild();
    }

    public int width(){return width;} public int height(){return height;}
    public Stone stoneAt(int x,int y){return stones[y][x];}
    public int moveNumber(){return moveNumber;} public int moveCount(){return moves.size();}
    public boolean lastMoveCreatedAtari(){return lastMoveCreatedAtari;}
    public Move lastMove(){return moveNumber==0?null:moves.get(moveNumber-1);}
    public Stone sideToMove(){
        if (sideToMoveOverride != null) return sideToMoveOverride;
        Move last=lastMove();
        return last==null?Stone.BLACK:last.stone().opposite();
    }

    public void loadMoves(List<Move> loaded) {
        loadRecord(List.of(), loaded, null);
    }

    public void loadRecord(List<SetupStone> setup, List<Move> loaded, Stone playerToMove) {
        moves.clear(); moves.addAll(loaded); moveNumber=moves.size();
        sideToMoveOverride = playerToMove;
        rebuild(setup);
    }

    public record SetupStone(int x, int y, Stone stone) { }

    public boolean play(int x,int y){return play(new Move(x,y,sideToMove()),true);}

    private boolean play(Move move, boolean truncate) {
        lastMoveCreatedAtari=false;
        if (move.x()<0 || move.y()<0) {
            if(truncate) while(moves.size()>moveNumber)moves.remove(moves.size()-1);
            moves.add(move); moveNumber++; return true;
        }
        int x=move.x(),y=move.y();
        if(!inside(x,y)||stones[y][x]!=Stone.EMPTY)return false;
        Stone opponent=move.stone().opposite();
        boolean[][] before=groupsInAtari(stones,opponent);
        Stone[][] candidate=copyBoard(stones);
        if(!applyMove(candidate,move))return false;
        if(moveNumber>0&&sameBoard(candidate,positionAfter(moveNumber-1)))return false;
        lastMoveCreatedAtari=hasNewAtari(before,groupsInAtari(candidate,opponent));
        if(truncate)while(moves.size()>moveNumber)moves.remove(moves.size()-1);
        moves.add(move);moveNumber++;copyInto(candidate,stones);return true;
    }

    public boolean previous(){if(moveNumber==0)return false;moveNumber--;rebuild();return true;}
    public boolean next(){if(moveNumber>=moves.size())return false;moveNumber++;rebuild();return true;}
    public boolean first(){if(moveNumber==0)return false;moveNumber=0;rebuild();return true;}
    public boolean last(){if(moveNumber==moves.size())return false;moveNumber=moves.size();rebuild();return true;}

    private void rebuild(){rebuild(List.of());}
    private void rebuild(List<SetupStone> setup){lastMoveCreatedAtari=false;clear(stones);for(SetupStone s:setup)if(inside(s.x(),s.y()))stones[s.y()][s.x()]=s.stone();for(int i=0;i<moveNumber;i++){Move m=moves.get(i);if(m.x()<0||m.y()<0)continue;if(!applyMove(stones,m))throw new IllegalStateException("recorded move is illegal: "+m);}}
    private Stone[][] positionAfter(int count){Stone[][] b=newBoard();for(int i=0;i<count;i++){Move m=moves.get(i);if(m.x()<0||m.y()<0)continue;if(!applyMove(b,m))throw new IllegalStateException("recorded move is illegal: "+m);}return b;}

    private boolean applyMove(Stone[][] board,Move move){int x=move.x(),y=move.y();if(!inside(x,y)||board[y][x]!=Stone.EMPTY)return false;board[y][x]=move.stone();Stone opponent=move.stone().opposite();boolean[][] checked=new boolean[height][width];for(int[]d:directions){int nx=x+d[0],ny=y+d[1];if(!inside(nx,ny)||checked[ny][nx]||board[ny][nx]!=opponent)continue;Group group=groupAt(board,nx,ny);for(Point p:group.stones())checked[p.y()][p.x()]=true;if(group.liberties()==0)for(Point p:group.stones())board[p.y()][p.x()]=Stone.EMPTY;}if(groupAt(board,x,y).liberties()==0){board[y][x]=Stone.EMPTY;return false;}return true;}
    private boolean[][] groupsInAtari(Stone[][] board,Stone color){boolean[][]a=new boolean[height][width],seen=new boolean[height][width];for(int y=0;y<height;y++)for(int x=0;x<width;x++){if(seen[y][x]||board[y][x]!=color)continue;Group g=groupAt(board,x,y);for(Point p:g.stones())seen[p.y()][p.x()]=true;if(g.liberties()==1)for(Point p:g.stones())a[p.y()][p.x()]=true;}return a;}
    private boolean hasNewAtari(boolean[][]b,boolean[][]a){for(int y=0;y<height;y++)for(int x=0;x<width;x++)if(a[y][x]&&!b[y][x])return true;return false;}
    private Group groupAt(Stone[][]board,int sx,int sy){Stone color=board[sy][sx];boolean[][]seen=new boolean[height][width],ls=new boolean[height][width];ArrayDeque<Point>q=new ArrayDeque<>();List<Point>group=new ArrayList<>();int liberties=0;q.add(new Point(sx,sy));seen[sy][sx]=true;while(!q.isEmpty()){Point p=q.removeFirst();group.add(p);for(int[]d:directions){int nx=p.x()+d[0],ny=p.y()+d[1];if(!inside(nx,ny))continue;if(board[ny][nx]==Stone.EMPTY){if(!ls[ny][nx]){ls[ny][nx]=true;liberties++;}}else if(board[ny][nx]==color&&!seen[ny][nx]){seen[ny][nx]=true;q.addLast(new Point(nx,ny));}}}return new Group(group,liberties);}
    private boolean inside(int x,int y){return x>=0&&x<width&&y>=0&&y<height;}
    private Stone[][]newBoard(){Stone[][]b=new Stone[height][width];clear(b);return b;}
    private void clear(Stone[][]b){for(Stone[]r:b)Arrays.fill(r,Stone.EMPTY);}
    private Stone[][]copyBoard(Stone[][]s){Stone[][]c=new Stone[height][width];copyInto(s,c);return c;}
    private void copyInto(Stone[][]s,Stone[][]t){for(int y=0;y<height;y++)System.arraycopy(s[y],0,t[y],0,width);}
    private boolean sameBoard(Stone[][]a,Stone[][]b){for(int y=0;y<height;y++)if(!Arrays.equals(a[y],b[y]))return false;return true;}
    private record Point(int x,int y){} private record Group(List<Point>stones,int liberties){}
}
