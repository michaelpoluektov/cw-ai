package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;

import java.util.Optional;
import java.util.Random;

public class MCTree {
    MCNode rootNode;
    public MCTree(Board board, Toml constants){
        rootNode = new MCNode(board, constants);
    }
    public MCNode getRootNode(){
        return this.rootNode;
    }
    public MCNode properGate(MCNode node){
        if(!node.getChildren().isEmpty()){
            Optional<MCNode> bestNode = Optional.empty();
            Double bestUTC = Double.NEGATIVE_INFINITY;
            for(MCNode child : node.getChildren()){
                if(child.UTCScore() > bestUTC) bestNode = Optional.of(child);
            }
            bestNode.ifPresent(this::properGate);
        }
        return node;
    }

    public void singleSimulation(){
        MCNode nodeToRollFrom = properGate(rootNode);
        nodeToRollFrom.rollout();
    }
}
