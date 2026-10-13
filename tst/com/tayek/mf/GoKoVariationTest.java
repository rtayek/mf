package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

final class GoKoVariationTest {
    private static final List<BoardPosition.SetupStone> KO = List.of(
        new BoardPosition.SetupStone(0,1,Stone.BLACK),
        new BoardPosition.SetupStone(2,1,Stone.BLACK),
        new BoardPosition.SetupStone(1,0,Stone.BLACK),
        new BoardPosition.SetupStone(1,1,Stone.WHITE),
        new BoardPosition.SetupStone(0,2,Stone.WHITE),
        new BoardPosition.SetupStone(2,2,Stone.WHITE),
        new BoardPosition.SetupStone(1,3,Stone.WHITE)
    );

    @Test void immediateKoRecaptureIsIllegalButPassesReleaseIt() {
        BoardPosition board=new BoardPosition(5,5);
        board.loadRecord(KO,List.of(),Stone.BLACK);
        assertTrue(board.play(1,2));
        assertEquals(Stone.EMPTY,board.stoneAt(1,1));
        assertEquals(Stone.BLACK,board.stoneAt(1,2));
        assertFalse(board.play(1,1));
        assertEquals(1,board.moveNumber());
        assertTrue(board.pass());
        assertTrue(board.pass());
        assertTrue(board.play(1,1));
        assertEquals(Stone.WHITE,board.stoneAt(1,1));
        assertEquals(Stone.EMPTY,board.stoneAt(1,2));
    }

    @Test void koWithSetupIsRestoredAcrossNavigation() {
        BoardPosition board=new BoardPosition(5,5);
        board.loadRecord(KO,List.of(new Move(1,2,Stone.BLACK)),null);
        assertFalse(board.play(1,1));
        assertTrue(board.previous());
        assertEquals(Stone.WHITE,board.stoneAt(1,1));
        assertEquals(Stone.EMPTY,board.stoneAt(1,2));
        assertTrue(board.next());
        assertEquals(Stone.BLACK,board.stoneAt(1,2));
        assertFalse(board.play(1,1));
    }

    @Test void variationsHaveIndependentPositionsAndSurviveRoundTrip() {
        GameCollection collection=SgfReader.read("(;GM[1]SZ[5];B[aa](;W[bb];B[bc])(;W[cc];B[cd])(;W[];B[dd]))");
        GameNode first=collection.games().get(0).tree().root().children().get(0);
        assertEquals(3,first.children().size());
        BoardPosition a=position(first.children().get(0).children().get(0));
        BoardPosition b=position(first.children().get(1).children().get(0));
        BoardPosition pass=position(first.children().get(2).children().get(0));
        assertEquals(Stone.WHITE,a.stoneAt(1,1));
        assertEquals(Stone.EMPTY,a.stoneAt(2,2));
        assertEquals(Stone.WHITE,b.stoneAt(2,2));
        assertEquals(Stone.EMPTY,b.stoneAt(1,1));
        assertEquals(Stone.BLACK,pass.stoneAt(3,3));
        assertEquals(Stone.EMPTY,pass.stoneAt(1,1));
        assertEquals(3,pass.moveNumber());
        String saved=SgfWriter.write(collection);
        GameCollection restored=SgfReader.read(saved);
        assertEquals(saved,SgfWriter.write(restored));
        GameNode root=restored.games().get(0).tree().root();
        assertEquals(3,root.children().get(0).children().size());
        assertEquals(-1,root.children().get(0).children().get(2).move().x());
        assertEquals(Stone.BLACK,position(root.children().get(0).children().get(2).children().get(0)).stoneAt(3,3));
    }

    private static BoardPosition position(GameNode node) {
        java.util.ArrayList<Move> moves=new java.util.ArrayList<>();
        for(GameNode n=node;n!=null;n=n.parent())if(n.move()!=null)moves.add(0,n.move());
        BoardPosition board=new BoardPosition(5,5);
        board.loadMoves(moves);
        return board;
    }
}
