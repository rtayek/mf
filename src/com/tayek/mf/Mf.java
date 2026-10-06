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
    private Label status; private Label welcome; private BorderPane content; private String gameLabel=""; private Game activeGame;
    private GameRuntime runtime;
    private GameNode currentNode; private GameNode rootNode;
    @Override public void start(Stage stage){status=new Label();status.setStyle("-fx-font-family: serif; -fx-font-size: 16px;");BorderPane.setAlignment(status,Pos.CENTER);BorderPane.setMargin(status,new Insets(4,0,12,0));welcome=new Label("Open an SGF file to begin");welcome.setStyle("-fx-font-family: serif; -fx-font-size: 22px;");BorderPane.setAlignment(welcome,Pos.CENTER);MenuItem open=new MenuItem("Open SGF...");open.setOnAction(e->openSgf(stage));Menu file=new Menu("File");file.getItems().add(open);MenuBar menuBar=new MenuBar(file);content=new BorderPane();content.setPadding(new Insets(0,0,8,0));content.setCenter(welcome);BorderPane root=new BorderPane();root.setTop(menuBar);root.setCenter(content);Scene scene=new Scene(root,760,800);scene.setOnKeyPressed(e->{if(rootNode==null)return;boolean changed=false;switch(e.getCode()){case LEFT->changed=treeBack();case RIGHT->changed=treeForward(0);case UP->changed=chooseSibling(-1);case DOWN->changed=chooseSibling(1);case HOME->changed=treeHome();case END->changed=treeEnd();default->{}}if(changed)e.consume();});stage.setTitle("mf - SGF Viewer");stage.setScene(scene);stage.show();}
    private void openSgf(Stage stage){FileChooser chooser=new FileChooser();chooser.setTitle("Open SGF Game");chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SGF files","*.sgf"));String savedDirectory=preferences.get(LAST_SGF_DIRECTORY,"");if(!savedDirectory.isBlank()){File directory=new File(savedDirectory);if(directory.isDirectory())chooser.setInitialDirectory(directory);}File file=chooser.showOpenDialog(stage);if(file==null)return;File parent=file.getParentFile();if(parent!=null&&parent.isDirectory())preferences.put(LAST_SGF_DIRECTORY,parent.getAbsolutePath());try{GameCollection collection=SgfReader.read(file.toPath());Game game=collection.games().stream().findFirst().orElseThrow(()->new IllegalArgumentException("SGF collection contains no games"));showGame(game);stage.setTitle("mf - "+game.name());}catch(Exception ex){new Alert(Alert.AlertType.ERROR,"Could not load SGF:\n"+ex.getMessage(),ButtonType.OK).showAndWait();}}
    private void showGame(Game game){activeGame=game;rootNode=game.tree().root();currentNode=rootNode;String players=game.blackPlayer()+" - "+game.whitePlayer();gameLabel=(game.name().isBlank()?players:game.name()+"    "+players);runtime=GameRuntime.forGame(game,this::updateStatus,this::selectVariation);content.setCenter(runtime.view());content.setBottom(status);reloadPath();}
    private List<GameNode> pathNodes(){ArrayList<GameNode> reversed=new ArrayList<>();for(GameNode n=currentNode;n!=null;n=n.parent())reversed.add(n);ArrayList<GameNode> result=new ArrayList<>();for(int i=reversed.size()-1;i>=0;i--)result.add(reversed.get(i));return result;}
    private void reloadPath(){if(runtime==null)return;runtime.showPath(pathNodes());updateStatus();}
    private void selectVariation(int child){if(currentNode==null||child<0||child>=currentNode.children().size())return;currentNode=currentNode.children().get(child);reloadPath();}
    private boolean treeBack(){if(currentNode==null||currentNode==rootNode)return false;currentNode=currentNode.parent();reloadPath();return true;}
    private boolean treeForward(int child){if(currentNode==null||currentNode.children().isEmpty())return false;currentNode=currentNode.children().get(Math.min(child,currentNode.children().size()-1));reloadPath();return true;}
    private boolean chooseSibling(int delta){if(currentNode==null||currentNode==rootNode||currentNode.parent()==null)return false;List<GameNode>s=currentNode.parent().children();if(s.size()<2)return false;int i=s.indexOf(currentNode),n=(i+delta+s.size())%s.size();currentNode=s.get(n);reloadPath();return true;}
    private boolean treeHome(){if(currentNode==null||currentNode==rootNode)return false;currentNode=rootNode;reloadPath();return true;}
    private boolean treeEnd(){if(currentNode==null)return false;boolean changed=false;while(!currentNode.children().isEmpty()){currentNode=currentNode.children().get(0);changed=true;}if(changed)reloadPath();return changed;}
    private void updateStatus(){if(activeGame==null||runtime==null){status.setText("");return;}int moveNumber=runtime.moveNumber();Stone side=runtime.sideToMove();String variation="";if(currentNode!=null&&currentNode.parent()!=null&&currentNode.parent().children().size()>1){int i=currentNode.parent().children().indexOf(currentNode)+1;variation="    Variation "+i+"/"+currentNode.parent().children().size();}status.setText(gameLabel+"    Move "+moveNumber+"    "+(side==Stone.BLACK?"Black":"White")+" to move"+variation);}
    public static void main(String[]args){launch(args);}
}
