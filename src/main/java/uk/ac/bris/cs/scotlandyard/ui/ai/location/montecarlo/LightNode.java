package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.swing.*;
import java.util.Random;

public class LightNode extends AbstractNode {
    protected LightNode(MiniBoard miniBoard,
                        AbstractNode parent,
                        NodeUCTComparator comparator) {
        super(miniBoard, parent, comparator);
    }

    /**
     * Standard implementation of the rollout method. This randomly selects a path to the bottom of the decision tree
     * @see HeavyNode for an implementation of a "heavy" playout.
     * @return Round at which the game has ended
     */
    @Override
    protected Integer rollout(IntermediateScore... intermediateScores) {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        if(rollingMiniBoard.getWinner() == MiniBoard.winner.MRX) return getRoundSize()+1;
        else return rollingMiniBoard.getRound();
    }

    @Override
    protected AbstractNode getNewNode(MiniBoard miniBoard, AbstractNode parent, NodeUCTComparator comparator) {
        return new LightNode(miniBoard, parent, comparator);
    }
}
