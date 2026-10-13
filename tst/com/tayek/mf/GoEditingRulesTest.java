package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

final class GoEditingRulesTest {
    @Test void passesSwitchPlayerAndRoundTripThroughSgf() {
        GameCollection games=SgfReader.read("(;GM[1]SZ[9])");
        GameNode root=games.games().get(0).tree().root();
        CommandHistory history=new CommandHistory();
        GameNode black=new GameNode(new Move(-1,-1,Stone.BLACK),root,
                List.of(new SgfProperty("B",List.of(""))));
        history.execute(new AddNodeCommand(root,black));
        GameNode white=new GameNode(new Move(-1,-1,Stone.WHITE),black,
                List.of(new SgfProperty("W",List.of(""))));
        history.execute(new AddNodeCommand(black,white));
        String saved=SgfWriter.write(games);
        assertEquals(saved,SgfWriter.write(SgfReader.read(saved)));
        assertTrue(history.undo());
        assertTrue(black.children().isEmpty());
        assertTrue(history.redo());
        assertSame(white,black.children().get(0));
        BoardPosition board=new BoardPosition(9,9);
        assertTrue(board.pass());
        assertEquals(Stone.WHITE,board.sideToMove());
        assertTrue(board.pass());
        assertEquals(Stone.BLACK,board.sideToMove());
        assertEquals(2,board.moveNumber());
    }

    @Test void captureAndSuicide() {
        BoardPosition board=new BoardPosition(5,5);
        board.loadRecord(List.of(
            new BoardPosition.SetupStone(0,1,Stone.BLACK),
            new BoardPosition.SetupStone(1,0,Stone.BLACK),
            new BoardPosition.SetupStone(2,1,Stone.BLACK),
            new BoardPosition.SetupStone(1,1,Stone.WHITE)
        ),List.of(),Stone.BLACK);
        assertTrue(board.play(1,2));
        assertEquals(Stone.EMPTY,board.stoneAt(1,1));
        assertFalse(board.play(1,1));
    }

    @Test void setupSurvivesNavigationAndKoHistory() {
        BoardPosition board=new BoardPosition(5,5);
        board.loadRecord(List.of(new BoardPosition.SetupStone(4,4,Stone.BLACK)),
                List.of(new Move(0,0,Stone.BLACK)),null);
        assertTrue(board.previous());
        assertEquals(Stone.BLACK,board.stoneAt(4,4));
        assertTrue(board.next());
        assertEquals(Stone.BLACK,board.stoneAt(4,4));
        assertEquals(Stone.WHITE,board.sideToMove());
    }
}
