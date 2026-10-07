# mf project knowledge index

This is a navigation aid, not governing instructions. Paths are relative to the repository root.

## SGF document and navigation
- `src/com/tayek/mf/SgfReader.java`: parse SGF collections and properties.
- `src/com/tayek/mf/GameNode.java`: SGF node, properties, parent and children. `property(id)` returns authored values.
- `src/com/tayek/mf/GameRuntime.java`: project SGF paths into game-specific displays. `GoRuntime.showPath` reconstructs the Go position.
- `src/com/tayek/mf/Mf.java`: open SGF, tree navigation, node names and comments.

## Go position and visual features
- `src/com/tayek/mf/BoardPosition.java`: Go position, move replay, setup stones and side to move.
- `src/com/tayek/mf/GameRuntime.java`:
  - `GoRuntime.setup` reads AB/AW/AE setup points.
  - `GoRuntime.playerToMove` reads PL.
  - `GoRuntime.labels` reads LB and creates clickable child-variation labels.
  - `GoRuntime.marks` reads TR/SQ/CR/MA on the current node.
- `src/com/tayek/mf/BoardView.java`:
  - `drawLabels` draws text and variation choices.
  - `drawMarks` draws triangles, squares, circles and X marks.
  - `playHover` handles clicks, variations and sounds.

## SGF point-range notation
- `TR[aa:cc]` is a *property value inside an SGF file*, not a source-code reference.
- It denotes all nine intersections of the inclusive rectangle from `aa` to `cc`.
- The current `GoRuntime.marks` implementation accepts only two-character point values such as `TR[aa][bb]`. It does not yet expand compressed rectangles such as `TR[aa:cc]`.
- This is an outstanding compatibility item; do not confuse it with variation labels.

## Audio
- `src/com/tayek/mf/BoardView.java`: `loadSound` currently loads `/audio/goclickb.wav` for stone placement and `/audio/goatari.wav` for atari/warnings.
- Audio files are expected on the runtime classpath under `audio/`. Additional files copied from RTGo have not yet been inventoried or assigned behaviors.

## Boundaries and future work
- `GameRuntime` is deliberately narrow, not a universal rules/geometry abstraction.
- SGF writer/round-trip, compressed point ranges, and richer audio routing are future work.
