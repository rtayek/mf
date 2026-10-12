package com.tayek.mf;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Reversible model operation. No UI or game rules belong here. */
interface Command {
    void execute();
    void undo();
}

/** Per-document history; navigation does not enter the history. */
final class CommandHistory {
    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();

    void execute(Command command) {
        Objects.requireNonNull(command).execute();
        undo.push(command);
        redo.clear();
    }

    boolean canUndo() { return !undo.isEmpty(); }
    boolean canRedo() { return !redo.isEmpty(); }
    Command nextRedo() { return redo.peek(); }

    boolean undo() {
        if (!canUndo()) return false;
        Command command = undo.peek();
        command.undo();
        undo.pop();
        redo.push(command);
        return true;
    }

    boolean redo() {
        if (!canRedo()) return false;
        Command command = redo.peek();
        command.execute();
        redo.pop();
        undo.push(command);
        return true;
    }

    void clear() { undo.clear(); redo.clear(); }
}

/** Generic tree edit; the caller creates and validates any game-specific move. */
final class AddNodeCommand implements Command {
    private final GameNode parent;
    private final GameNode child;
    private final int index;

    AddNodeCommand(GameNode parent, GameNode child) {
        this(parent, child, Objects.requireNonNull(parent).children().size());
    }

    AddNodeCommand(GameNode parent, GameNode child, int index) {
        this.parent = Objects.requireNonNull(parent);
        this.child = Objects.requireNonNull(child);
        this.index = index;
        if (child.parent() != parent) throw new IllegalArgumentException("child has different parent");
        if (index < 0 || index > parent.children().size()) throw new IndexOutOfBoundsException(index);
        if (parent.children().contains(child)) throw new IllegalArgumentException("child already attached");
    }

    GameNode parent() { return parent; }
    GameNode child() { return child; }

    @Override public void execute() { parent.insertChild(index, child); }
    @Override public void undo() { parent.removeChild(child); }
}
