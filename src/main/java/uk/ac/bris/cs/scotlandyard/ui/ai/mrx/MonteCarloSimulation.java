package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sun.source.tree.Tree;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;


public class MonteCarloSimulation {
    Treenode parentNode;
    MiniBoard miniBoard;

    public MonteCarloSimulation(MiniBoard miniBoard) {
        this.parentNode = new Treenode(miniBoard);
        this.miniBoard = miniBoard;
    }


    public Treenode findBestNode(Treenode startNode){
        if(startNode.children.isEmpty()) throw new NoSuchElementException();
        Treenode bestTreeNode = null;
        for(Treenode treenode : startNode.children){
            if(treenode.UTCScore() > bestTreeNode.UTCScore()) bestTreeNode = treenode;
        }
        return bestTreeNode;
    }
    public void runSingleSimulation(Treenode currentTreeNode){
        while(currentTreeNode.fullyExpanded()) {
            Treenode bestPath = findBestNode(currentTreeNode);
            runSingleSimulation(bestPath);
        }
        currentTreeNode.miniBoard.






    }














}
