package com.tayek.mf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Mf extends Application {
    private static final String LAST_SGF_DIRECTORY = "lastSgfDirectory";
    private final Preferences preferences = Preferences.userNodeForPackage(Mf.class);
    private Label status; private Label welcome; private BorderPane content; private VBox documentPane; private Label nodeName; private TextArea comment; private String gameLabel=""; private Game activeGame; private GameCollection activeCollection; private File sourceFile; private MenuItem saveAs, saveItem; private Stage mainStage;
    private GameRuntime runtime; private CheckMenuItem showSingleContinuation;
    private GameNode currentNode; private GameNode rootNode;
    private final CommandHistory editHistory = new CommandHistory();
    private MenuItem undoItem, redoItem, deleteBranchItem, passItem;
    private TreeView<GameNode> gameTreeView;
    private CheckMenuItem showGameTree;
    private boolean syncingTree;
    @Override public void start(Stage stage){mainStage=stage;status=new Label();status.setStyle("-fx-font-family: serif; -fx-font-size: 16px;");BorderPane.setAlignment(status,Pos.CENTER);BorderPane.setMargin(status,new Insets(4,0,12,0));welcome=new Label("Open an SGF file to begin");welcome.setStyle("-fx-font-family: serif; -fx-font-size: 22px;");BorderPane.setAlignment(welcome,Pos.CENTER);MenuItem open=new MenuItem("Open SGF...");open.setOnAction(e->openSgf(stage));Menu file=new Menu("File");saveAs=new MenuItem("Save As...");saveAs.setDisable(true);saveAs.setOnAction(e->saveAs(stage));saveItem=new MenuItem("Save");saveItem.setDisable(true);saveItem.setAccelerator(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.S,javafx.scene.input.KeyCombination.SHORTCUT_DOWN));saveItem.setOnAction(e->save(stage));file.getItems().addAll(open,saveItem,saveAs);Menu viewMenu=new Menu("View");showSingleContinuation=new CheckMenuItem("Show single next-move label");showSingleContinuation.setSelected(true);showSingleContinuation.setOnAction(e->{if(runtime!=null){runtime.setShowSingleContinuation(showSingleContinuation.isSelected());reloadPath();}});viewMenu.getItems().add(showSingleContinuation);showGameTree=new CheckMenuItem("Show game tree");showGameTree.setSelected(true);showGameTree.setOnAction(e->updateTreeVisibility());viewMenu.getItems().add(showGameTree);Menu editMenu=new Menu("Edit");undoItem=new MenuItem("Undo");redoItem=new MenuItem("Redo");undoItem.setAccelerator(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.Z,javafx.scene.input.KeyCombination.SHORTCUT_DOWN));redoItem.setAccelerator(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.Y,javafx.scene.input.KeyCombination.SHORTCUT_DOWN));undoItem.setOnAction(e->undoEdit());redoItem.setOnAction(e->redoEdit());deleteBranchItem=new MenuItem("Delete Branch");deleteBranchItem.setAccelerator(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.DELETE));deleteBranchItem.setOnAction(e->deleteBranch());passItem=new MenuItem("Pass");passItem.setOnAction(e->passMove());editMenu.getItems().addAll(undoItem,redoItem,new SeparatorMenuItem(),deleteBranchItem,passItem);updateEditMenu();Menu helpMenu=new Menu("Help");MenuItem helpItem=new MenuItem("Keyboard and Mouse Help");helpItem.setOnAction(e->showHelp(stage));helpMenu.getItems().add(helpItem);MenuBar menuBar=new MenuBar(file,editMenu,viewMenu,helpMenu);content=new BorderPane();content.setPadding(new Insets(0,0,8,0));content.setCenter(welcome);nodeName=new Label();nodeName.setWrapText(true);nodeName.setStyle("-fx-font-family: serif; -fx-font-size: 18px; -fx-font-weight: bold;");comment=new TextArea();comment.setEditable(false);comment.setWrapText(true);comment.setStyle("-fx-font-family: serif; -fx-font-size: 16px;");VBox.setVgrow(comment,Priority.ALWAYS);documentPane=new VBox(8,nodeName,comment);documentPane.setPadding(new Insets(12));documentPane.setPrefWidth(300);documentPane.setMinWidth(220);
        gameTreeView=new TreeView<>();
        gameTreeView.setPrefWidth(230);
        gameTreeView.setMinWidth(170);
        gameTreeView.setShowRoot(true);
        gameTreeView.setCellFactory(tree->new TreeCell<>() {
            @Override protected void updateItem(GameNode node,boolean empty) {
                super.updateItem(node,empty);
                if(empty||node==null){setText(null);return;}
                Move move=node.move();
                if(move==null){setText("Root");return;}
                String side=move.stone()==Stone.BLACK?"B":"W";
                setText(move.x()<0?"Pass ("+side+")":side+" ["+((char)('a'+move.x()))+((char)('a'+move.y()))+"]");
            }
        });
        gameTreeView.getSelectionModel().selectedItemProperty().addListener((obs,oldItem,item)->{
            if(syncingTree||item==null||item.getValue()==null)return;
            currentNode=item.getValue();
            reloadPath();
        });
        BorderPane root=new BorderPane();root.setTop(menuBar);root.setCenter(content);Scene scene=new Scene(root,760,800);scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,e->{
        if(e.getCode()==javafx.scene.input.KeyCode.F1){showHelp(stage);e.consume();return;}
        if(e.isControlDown() && !e.isAltDown() && !e.isMetaDown()){
            if(e.getCode()==javafx.scene.input.KeyCode.Z){
                if(e.isShiftDown())redoEdit();else undoEdit();
                e.consume();return;
            }
            if(e.getCode()==javafx.scene.input.KeyCode.Y){redoEdit();e.consume();return;}
            if(e.getCode()==javafx.scene.input.KeyCode.S){save(stage);e.consume();return;}
        }
        if(rootNode==null||e.isControlDown()||e.isAltDown()||e.isMetaDown())return;
        if(e.getCode()==javafx.scene.input.KeyCode.DELETE){deleteBranch();e.consume();return;}
        boolean changed=false;switch(e.getCode()){case LEFT->changed=treeBack();case RIGHT->changed=treeForward(0);case UP->changed=chooseSibling(-1);case DOWN->changed=chooseSibling(1);case HOME->changed=treeHome();case END->changed=treeEnd();default->{}}if(changed)e.consume();});stage.setTitle("mf - SGF Viewer");stage.setOnCloseRequest(e->{if(!confirmDiscard(stage))e.consume();});stage.setScene(scene);stage.show();}
    private void showHelp(Stage stage) {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.initOwner(stage);
        help.setTitle("MF Help");
        help.setHeaderText("Keyboard and mouse controls");
        help.setContentText("""
                Editing
                  Ctrl+S             Save current file\n                  File > Save As     Save a separate copy\n                  Ctrl+Z             Undo an added move
                  Ctrl+Y             Redo an undone move
                  Ctrl+Shift+Z       Redo (alternative)
                  Edit > Pass        Add a Go pass move\n                  Delete             Delete selected branch (and descendants)\n                  Ctrl+Z             Restore a deleted branch

                Navigation
                  Left / Right       Previous / next move
                  Up / Down          Previous / next variation
                  Home / End         Root / end of main line\n                  Game tree          Click a node to navigate

                Mouse
                  Click empty point  Add a legal Go move
                  Click a variation  Follow that variation
                  Hover over board   Preview a move or variation

                F1                   Show this help
                """);
        help.getDialogPane().setMinWidth(520);
        help.showAndWait();
    }
    private void openSgf(Stage stage){if(!confirmDiscard(stage))return;FileChooser chooser=new FileChooser();chooser.setTitle("Open SGF Game");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SGF files","*.sgf"));String savedDirectory=preferences.get(LAST_SGF_DIRECTORY,"");if(!savedDirectory.isBlank()){File directory=new File(savedDirectory);if(directory.isDirectory())chooser.setInitialDirectory(directory);}File file=chooser.showOpenDialog(stage);if(file==null)return;File parent=file.getParentFile();if(parent!=null&&parent.isDirectory())preferences.put(LAST_SGF_DIRECTORY,parent.getAbsolutePath());try{GameCollection collection=SgfReader.read(file.toPath());Game game=collection.games().stream().findFirst().orElseThrow(()->new IllegalArgumentException("SGF collection contains no games"));showGame(game);activeCollection=collection;sourceFile=file;saveAs.setDisable(false);saveItem.setDisable(false);updateTitle();}catch(Exception ex){new Alert(Alert.AlertType.ERROR,"Could not load SGF:\n"+ex.getMessage(),ButtonType.OK).showAndWait();}}
    private boolean confirmDiscard(Stage stage) {
        if(!editHistory.isModified())return true;
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION,
                "The current game has unsaved changes. Discard them?",
                ButtonType.YES,ButtonType.NO);
        alert.initOwner(stage);
        alert.setHeaderText("Unsaved changes");
        return alert.showAndWait().orElse(ButtonType.NO)==ButtonType.YES;
    }
    private void updateTitle() {
        if(mainStage==null)return;
        String name=sourceFile!=null?sourceFile.getName():"SGF Viewer";
        mainStage.setTitle("mf - "+name+(editHistory.isModified()?" *":""));
    }
    private void save(Stage stage) {
        if(activeCollection==null)return;
        if(sourceFile==null){saveAs(stage);return;}
        try {
            SgfSave.saveAs(activeCollection,sourceFile.toPath());
            editHistory.markSaved();
            updateTitle();
        } catch(Exception ex) {
            Alert error=new Alert(Alert.AlertType.ERROR,"Could not save SGF:\n"+ex.getMessage(),ButtonType.OK);
            error.initOwner(stage);
            error.showAndWait();
        }
    }
    private void saveAs(Stage stage){
        if(activeCollection==null)return;
        FileChooser chooser=new FileChooser();
        chooser.setTitle("Save SGF Collection As");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SGF files","*.sgf"));
        chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));
        String savedDirectory=preferences.get(LAST_SGF_DIRECTORY,"");
        File directory=sourceFile!=null?sourceFile.getParentFile():null;
        if(directory==null||!directory.isDirectory())directory=new File(savedDirectory);
        if(directory.isDirectory())chooser.setInitialDirectory(directory);
        if(sourceFile!=null)chooser.setInitialFileName(sourceFile.getName());
        File selected=chooser.showSaveDialog(stage);
        if(selected==null)return;
        File target=selected;
        if(!target.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".sgf"))
            target=new File(target.getParentFile(),target.getName()+".sgf");
        if(target.exists()){
            Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,
                    "Overwrite existing file?\n"+target.getAbsolutePath(),
                    ButtonType.YES,ButtonType.NO);
            confirm.initOwner(stage);
            if(confirm.showAndWait().orElse(ButtonType.NO)!=ButtonType.YES)return;
        }
        try{
            SgfSave.saveAs(activeCollection,target.toPath());
            File parent=target.getParentFile();
            if(parent!=null&&parent.isDirectory())preferences.put(LAST_SGF_DIRECTORY,parent.getAbsolutePath());
            sourceFile=target;
            editHistory.markSaved();
            updateTitle();
        }catch(Exception ex){
            Alert error=new Alert(Alert.AlertType.ERROR,"Could not save SGF:\n"+ex.getMessage(),ButtonType.OK);
            error.initOwner(stage);
            error.showAndWait();
        }
    }
    private void showGame(Game game){editHistory.clear();updateEditMenu();activeGame=game;rootNode=game.tree().root();currentNode=rootNode;String players=game.blackPlayer()+" - "+game.whitePlayer();gameLabel=(game.name().isBlank()?players:game.name()+"    "+players);runtime=GameRuntime.forGame(game,this::updateStatus,this::selectVariation,this::playMove);runtime.setShowSingleContinuation(showSingleContinuation.isSelected());content.setLeft(documentPane);content.setCenter(runtime.view());content.setBottom(status);updateTreeVisibility();reloadPath();}
    private List<GameNode> pathNodes(){ArrayList<GameNode> reversed=new ArrayList<>();for(GameNode n=currentNode;n!=null;n=n.parent())reversed.add(n);ArrayList<GameNode> result=new ArrayList<>();for(int i=reversed.size()-1;i>=0;i--)result.add(reversed.get(i));return result;}
    private void reloadPath(){if(runtime==null)return;runtime.showPath(pathNodes());updateDocumentPane();updateStatus();updateEditMenu();updateTitle();refreshTree();}
    private void updateTreeVisibility() {
        if(content!=null&&showGameTree!=null)content.setRight(showGameTree.isSelected()?gameTreeView:null);
    }
    private TreeItem<GameNode> treeItem(GameNode node) {
        TreeItem<GameNode> item=new TreeItem<>(node);
        item.setExpanded(true);
        for(GameNode child:node.children())item.getChildren().add(treeItem(child));
        return item;
    }
    private TreeItem<GameNode> findTreeItem(TreeItem<GameNode> item,GameNode node) {
        if(item==null)return null;
        if(item.getValue()==node)return item;
        for(TreeItem<GameNode> child:item.getChildren()){
            TreeItem<GameNode> found=findTreeItem(child,node);
            if(found!=null)return found;
        }
        return null;
    }
    private void refreshTree() {
        if(gameTreeView==null||rootNode==null)return;
        syncingTree=true;
        try {
            TreeItem<GameNode> root=treeItem(rootNode);
            gameTreeView.setRoot(root);
            TreeItem<GameNode> selected=findTreeItem(root,currentNode);
            if(selected!=null)gameTreeView.getSelectionModel().select(selected);
        } finally {syncingTree=false;}
    }
    private void updateDocumentPane(){if(currentNode==null){nodeName.setText("");comment.setText("");return;}nodeName.setText(String.join("\n",currentNode.property("N")));comment.setText(String.join("\n\n",currentNode.property("C")));comment.positionCaret(0);}
    private void selectVariation(int child){if(currentNode==null||child<0||child>=currentNode.children().size())return;currentNode=currentNode.children().get(child);reloadPath();}
    private void playMove(int x, int y) {
        if (activeGame == null || !activeGame.type().equals(GameType.GO) || currentNode == null) return;
        Stone side = runtime.sideToMove();
        for (GameNode child : currentNode.children()) {
            Move move = child.move();
            if (move != null && move.x() == x && move.y() == y && move.stone() == side) {
                currentNode = child;
                reloadPath();
                return;
            }
        }
        if (!runtime.tryPlay(x, y)) return;
        String point = "" + (char)('a' + x) + (char)('a' + y);
        GameNode child = new GameNode(new Move(x, y, side), currentNode,
                List.of(new SgfProperty(side == Stone.BLACK ? "B" : "W", List.of(point))));
        editHistory.execute(new AddNodeCommand(currentNode, child));
        currentNode = child;
        reloadPath();
        updateEditMenu();
    }
    private void passMove() {
        if(activeGame==null || activeGame.type()!=GameType.GO || currentNode==null)return;
        Stone side=runtime.sideToMove();
        for(GameNode child:currentNode.children()) {
            Move move=child.move();
            if(move!=null && move.x()<0 && move.y()<0 && move.stone()==side) {
                currentNode=child;reloadPath();return;
            }
        }
        GameNode child=new GameNode(new Move(-1,-1,side),currentNode,
                List.of(new SgfProperty(side==Stone.BLACK?"B":"W",List.of(""))));
        editHistory.execute(new AddNodeCommand(currentNode,child));
        currentNode=child;
        reloadPath();
    }
    private void updateEditMenu() {
        if(passItem!=null)passItem.setDisable(activeGame==null || activeGame.type()!=GameType.GO);
        if (deleteBranchItem != null) deleteBranchItem.setDisable(currentNode == null || currentNode == rootNode);
        if (undoItem != null) undoItem.setDisable(!editHistory.canUndo());
        if (redoItem != null) redoItem.setDisable(!editHistory.canRedo());
    }
    private void deleteBranch() {
        if (currentNode == null || currentNode == rootNode) return;
        GameNode parent = currentNode.parent();
        editHistory.execute(new RemoveBranchCommand(currentNode));
        currentNode = parent;
        reloadPath();
        updateEditMenu();
    }
    private void undoEdit() {
        Command pending = editHistory.nextUndo();
        if (editHistory.undo()) {
            if (pending instanceof RemoveBranchCommand removed) currentNode = removed.branch();
            while (currentNode != null && !isAttachedToRoot(currentNode))
                currentNode = currentNode.parent();
            if (currentNode == null) currentNode = rootNode;
            reloadPath();
        }
        updateEditMenu();
        updateTitle();
    }
    private void redoEdit() {
        Command pending = editHistory.nextRedo();
        if (editHistory.redo()) {
            if (pending instanceof AddNodeCommand add) currentNode = add.child();
            else if (pending instanceof RemoveBranchCommand removed) currentNode = removed.parent();
            reloadPath();
        }
        updateEditMenu();
    }
    private boolean isAttachedToRoot(GameNode node) {
        while (node != rootNode) {
            GameNode parent = node.parent();
            if (parent == null || !parent.children().contains(node)) return false;
            node = parent;
        }
        return rootNode != null;
    }
    private boolean treeBack(){if(currentNode==null||currentNode==rootNode)return false;currentNode=currentNode.parent();reloadPath();return true;}
    private boolean treeForward(int child){if(currentNode==null||currentNode.children().isEmpty())return false;currentNode=currentNode.children().get(Math.min(child,currentNode.children().size()-1));reloadPath();return true;}
    private boolean chooseSibling(int delta){if(currentNode==null||currentNode==rootNode||currentNode.parent()==null)return false;List<GameNode>s=currentNode.parent().children();if(s.size()<2)return false;int i=s.indexOf(currentNode),n=(i+delta+s.size())%s.size();currentNode=s.get(n);reloadPath();return true;}
    private boolean treeHome(){if(currentNode==null||currentNode==rootNode)return false;currentNode=rootNode;reloadPath();return true;}
    private boolean treeEnd(){if(currentNode==null)return false;boolean changed=false;while(!currentNode.children().isEmpty()){currentNode=currentNode.children().get(0);changed=true;}if(changed)reloadPath();return changed;}
    private void updateStatus(){if(activeGame==null||runtime==null){status.setText("");return;}int moveNumber=runtime.moveNumber();Stone side=runtime.sideToMove();String variation="";if(currentNode!=null&&currentNode.parent()!=null&&currentNode.parent().children().size()>1){int i=currentNode.parent().children().indexOf(currentNode)+1;variation="    Variation "+i+"/"+currentNode.parent().children().size();}status.setText(gameLabel+"    Move "+moveNumber+"    "+(side==Stone.BLACK?"Black":"White")+" to move"+variation);}
    public static void main(String[]args){launch(args);}
}
