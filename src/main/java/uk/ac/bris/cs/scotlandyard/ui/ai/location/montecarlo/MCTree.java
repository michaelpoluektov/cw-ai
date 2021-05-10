package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Collections;

public class MCTree {
    private final MCNode rootNode;

    protected MCTree(Board board, Toml constants){
        rootNode = new MCNode(board, constants);
    }

    protected MCNode selectNode(MCNode node){ // what
        MCNode selectedNode = rootNode;
        while(!selectedNode.isLeaf()) {
            selectedNode = Collections.max(selectedNode.getChildren());
        }
        return selectedNode;
    }

    protected MCNode getRootNode() {
        return rootNode;
    }

    protected void runSimulation() {
        MCNode selectedNode = selectNode(rootNode);
        selectedNode.expand();
        if(!selectedNode.getChildren().isEmpty()) {
            MCNode selectedChild = selectedNode.getChildren().asList().get(0);
            Boolean rolloutResult = selectedChild.rollout();
            selectedChild.backPropagate(rolloutResult);
        } else {
            MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
            if(winner == MiniBoard.winner.NONE) throw new RuntimeException("State with no children has no winner!");
            selectedNode.backPropagate(winner == MiniBoard.winner.MRX);
        }
    }
}
