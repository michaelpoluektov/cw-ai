package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.*;

/**
 * Node using the "heavy" playout method. Expects each agent to pick the best move based on the provided heuristics.
 * Has not yet been tested extensively, but it appears like this method is more effective with detectives being further
 * away from MrX, while {@link LightNode} is more effective in case the detectives are close by.
 * @see LightNode
 * @see AbstractNode
 */

@Beta public class HeavyNode extends AbstractNode {
    protected HeavyNode(MiniBoard miniBoard,
                        AbstractNode parent,
                        NodeUCTComparator comparator) {
        super(miniBoard, parent, comparator);
    }

    /**
     * Rollout using the "heavy" method. This takes significantly more time than any other rollout method, and therefore
     * can greatly benefit from multithreading.
     * @param intermediateScores Heuristic functions to use in the rollout process, can be empty
     */
    @Override
    protected Integer rollout(IntermediateScore... intermediateScores) {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        Comparator<MiniBoard> comparator = new MiniBoard.ScoreComparator(intermediateScores);
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            if(rollingMiniBoard.getMrXToMove()) {
                rollingMiniBoard = Collections.max(availableMiniBoards, comparator);
            } else rollingMiniBoard = Collections.min(availableMiniBoards, comparator);
        }
        if(rollingMiniBoard.getWinner() == MiniBoard.winner.MRX) return getRoundSize()+1;
        else return rollingMiniBoard.getRound();
    }

    @Override
    protected AbstractNode getNewNode(MiniBoard miniBoard, AbstractNode parent, NodeUCTComparator comparator) {
        return new HeavyNode(miniBoard, parent, comparator);
    }
}
