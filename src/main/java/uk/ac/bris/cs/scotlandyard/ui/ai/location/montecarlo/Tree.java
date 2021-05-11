package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Collections;

public class Tree {
    private final Node rootNode;

    protected Tree(Board board, Toml constants){
        rootNode = new Node(board, constants);
    }

    protected Node selectNode(Node node){ // what
        Node selectedNode = rootNode;
        while(!selectedNode.isLeaf()) {
            selectedNode = Collections.max(selectedNode.getChildren());
        }
        return selectedNode;
    }

    protected Node getRootNode() {
        return rootNode;
    }

    protected void runSimulation() {
        Node selectedNode = selectNode(rootNode);
        selectedNode.expand();
        if(!selectedNode.getChildren().isEmpty()) {
            Node selectedChild = selectedNode.getChildren().asList().get(0);
            Boolean rolloutResult = selectedChild.rollout();
            selectedChild.backPropagate(rolloutResult);
        } else {
            MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
            if(winner == MiniBoard.winner.NONE) throw new RuntimeException("State with no children has no winner!");
            selectedNode.backPropagate(winner == MiniBoard.winner.MRX);
        }
    }
}
