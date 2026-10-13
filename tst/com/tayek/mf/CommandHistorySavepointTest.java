package com.tayek.mf;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
final class CommandHistorySavepointTest {
 @Test void tracksSavepointAcrossUndoRedoAndNewBranch() {
  GameNode root=SgfReader.read("(;GM[1]SZ[9])").games().get(0).tree().root();
  CommandHistory h=new CommandHistory();
  assertFalse(h.isModified());
  GameNode a=new GameNode(new Move(0,0,Stone.BLACK),root);
  h.execute(new AddNodeCommand(root,a));
  assertTrue(h.isModified());
  h.markSaved();
  assertFalse(h.isModified());
  h.undo();
  assertTrue(h.isModified());
  h.redo();
  assertFalse(h.isModified());
  h.undo();
  GameNode b=new GameNode(new Move(1,1,Stone.BLACK),root);
  h.execute(new AddNodeCommand(root,b));
  assertTrue(h.isModified());
  assertFalse(h.canRedo());
  h.clear();
  assertFalse(h.isModified());
 }
}
