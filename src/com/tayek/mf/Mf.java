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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Mf extends Application {
    private static final String LAST_SGF_DIRECTORY = "lastSgfDirectory";
    private final Preferences preferences = Preferences.userNodeForPackage(Mf.class);
    private BoardPosition position=new BoardPosition(19,19); private BoardView boardView; private Label status; private String gameLabel="";
    private GameNode currentNode; private GameNode rootNode;
    @Override public void start(Stage stage){boardView=new BoardView(position);status=new Label();status.setStyle("-fx-font-family: serif; -fx-font-size: 16px;");BorderPane.setAlignment(status,Pos.CENTER);BorderPane.setMargin(status,new Insets(4,0,12,0));boardView.setOnPositionChanged(this::updateStatus);updateStatus();MenuItem open=new MenuItem("Open SGF...");open.setOnAction(e->openSgf(stage));Menu file=new Menu("File");file.getItems().add(open);MenuBar menuBar=new MenuBar(file);BorderPane content=new BorderPane();content.setPadding(new Insets(0,0,8,0));content.setCenter(boardView);content.setBottom(status);BorderPane root=new BorderPane();root.setTop(menuBar);root.setCenter(content);Scene scene=new Scene(root,760,800);scene.setOnKeyPressed(e->{boolean changed=false;if(rootNode!=null){switch(e.getCode()){case LEFT->changed=treeBack();case RIGHT->changed=treeForward(0);case UP->changed=chooseSibling(-1);case DOWN->changed=chooseSibling(1);case HOME->changed=treeHome();case END->changed=treeEnd();default->{}}}else{changed=switch(e.getCode()){case LEFT->position.previous();case RIGHT->position.next();case HOME->position.first();case END->position.last();default->false;};}if(changed){boardView.refresh();e.consume();}});stage.setTitle("mf - Go Viewer");stage.setScene(scene);stage.show();}
    private void openSgf(Stage stage){FileChooser chooser=new FileChooser();chooser.setTitle("Open SGF Game");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SGF files","*.sgf"));String savedDirectory=preferences.get(LAST_SGF_DIRECTORY,"");if(!savedDirectory.isBlank()){File directory=new File(savedDirectory);if(directory.isDirectory())chooser.setInitialDirectory(directory);}File file=chooser.showOpenDialog(stage);if(file==null)return;File parent=file.getParentFile();if(parent!=null&&parent.isDirectory())preferences.put(LAST_SGF_DIRECTORY,parent.getAbsolutePath());try{GameCollection collection=SgfReader.read(file.toPath());Game game=collection.gamesOfType(GameType.GO).stream().findFirst().orElseThrow(()->new IllegalArgumentException("SGF collection contains no Go game"));rootNode=game.tree().root();currentNode=rootNode;loadPath(game.boardSize());String players=game.blackPlayer()+" - "+game.whitePlayer();gameLabel=(game.name().isBlank()?players:game.name()+"    "+players);updateStatus();stage.setTitle("mf - "+game.name());}catch(Exception ex){new Alert(Alert.AlertType.ERROR,"Could not load SGF:\n"+ex.getMessage(),ButtonType.OK).showAndWait();}}
    private List<Move> pathMoves(){ArrayList<Move> reversed=new ArrayList<>();for(GameNode n=currentNode;n!=null;n=n.parent())if(n.move()!=null)reversed.add(n.move());ArrayList<Move> result=new ArrayList<>();for(int i=reversed.size()-1;i>=0;i--)result.add(reversed.get(i));return result;}
    private List<BoardLabel> currentLabels(){if(currentNode==null)return List.of();ArrayList<BoardLabel> result=new ArrayList<>();for(String value:currentNode.property("LB")){int colon=value.indexOf(':');if(colon<2)continue;int x=value.charAt(0)-'a',y=value.charAt(1)-'a';String text=value.substring(colon+1);if(x>=0&&x<position.width()&&y>=0&&y<position.height()&&!text.isEmpty())result.add(new BoardLabel(x,y,text));}return result;}
    private void loadPath(int size){BoardPosition loaded=new BoardPosition(size,size);loaded.loadMoves(pathMoves());position=loaded;boardView.setPosition(loaded);boardView.setLabels(currentLabels());}
    private void reloadPath(){loadPath(position.width());}
    private boolean treeBack(){if(currentNode==null||currentNode==rootNode)return false;currentNode=currentNode.parent();reloadPath();return true;}
    private boolean treeForward(int child){if(currentNode==null||currentNode.children().isEmpty())return false;currentNode=currentNode.children().get(Math.min(child,currentNode.children().size()-1));reloadPath();return true;}
    private boolean chooseSibling(int delta){if(currentNode==null||currentNode==rootNode||currentNode.parent()==null)return false;List<GameNode>s=currentNode.parent().children();if(s.size()<2)return false;int i=s.indexOf(currentNode),n=(i+delta+s.size())%s.size();currentNode=s.get(n);reloadPath();return true;}
    private boolean treeHome(){if(currentNode==rootNode)return false;currentNode=rootNode;reloadPath();return true;}
    private boolean treeEnd(){if(currentNode==null)return false;boolean changed=false;while(!currentNode.children().isEmpty()){currentNode=currentNode.children().get(0);changed=true;}if(changed)reloadPath();return changed;}
    private void updateStatus(){String side=position.sideToMove()==Stone.BLACK?"Black":"White";String prefix=gameLabel.isBlank()?"":gameLabel+"    ";String variation="";if(currentNode!=null&&currentNode.parent()!=null&&currentNode.parent().children().size()>1){int i=currentNode.parent().children().indexOf(currentNode)+1;variation="    Variation "+i+"/"+currentNode.parent().children().size();}status.setText(prefix+"Move "+position.moveNumber()+"    "+side+" to move"+variation);}
    public static void main(String[]args){launch(args);}
}
