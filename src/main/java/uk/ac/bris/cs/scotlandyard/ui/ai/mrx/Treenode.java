package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Treenode {
    public Move move;//move used to get to this current board
    public Integer location;
    public MiniBoard miniBoard;
    public Double n_plays;
    public Double n_wins;
    public Treenode parent;
    public List<Treenode> children;

    public Boolean isRoot(){
        return parent == null;
    }
    /*public Boolean isLeaf(){
        return children.size() == 0;
    }*/

    public Treenode(MiniBoard miniBoard) {
        this.miniBoard = miniBoard;
        this.children = new LinkedList<Treenode>();
        this.n_plays = 0.0;
        this.n_wins = 0.0;
        this.parent = null;

    }
    public Treenode addChildNode(MiniBoard miniBoard, Move move){
        Treenode child = new Treenode(miniBoard);
        child.parent = this;
        this.children.add(child);
        return child;
    }
    public int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }
    public Boolean fullyExpanded(){
        Boolean checkIfFull = miniBoard.getNodeDestinations(getLocationOfPersonToMove()).size() == children.size();
        return checkIfFull;
    }
    public Integer getLocationOfPersonToMove(){
        if(miniBoard.getMrXToMove()){
            return miniBoard.getMrXLocation();
        }
        else{
            return miniBoard.getDetectiveLocations().get(miniBoard.getDetectiveLocations().size() -1);
        }
    }
    public Double UTCScore(){
        if(n_plays == 0) return Double.POSITIVE_INFINITY;
        Double ratioWinToPlay = this.n_wins/ this.n_plays;
        Double explorationComponent = Math.sqrt(2) * Math.sqrt(Math.log((parent.n_plays)/this.n_plays));
        return ratioWinToPlay + explorationComponent;
    }





}
