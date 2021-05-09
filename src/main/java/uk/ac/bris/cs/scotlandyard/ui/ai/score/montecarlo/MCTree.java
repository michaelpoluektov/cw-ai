package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.NoSuchElementException;
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

        MCNode nodeToContinue = node;
        while(!nodeToContinue.isLeaf() && nodeToContinue.getMiniBoard().getWinner() == MiniBoard.winner.NONE){
            Optional<MCNode> bestNode = Optional.empty();
            Double bestUTC = Double.NEGATIVE_INFINITY;
            for(MCNode child : nodeToContinue.getChildren()){
                Double score = child.UTCScore();
                if(score > bestUTC){
                    bestUTC = score;
                    bestNode = Optional.of(child);
                }
            }
            if(bestNode.isEmpty()) throw new NoSuchElementException("No best move!");
            nodeToContinue = bestNode.get();
        }
        if(nodeToContinue.getPlays() == 0 || !(nodeToContinue.getMiniBoard().getWinner() == MiniBoard.winner.NONE)) return nodeToContinue;
        else{
            nodeToContinue.populateChildren();
            return nodeToContinue.getChildren().asList().get(0);
        }

    }

    public void singleSimulation(){
        MCNode nodeToRollFrom = properGate(rootNode);
        nodeToRollFrom.rollout();

    }
}
