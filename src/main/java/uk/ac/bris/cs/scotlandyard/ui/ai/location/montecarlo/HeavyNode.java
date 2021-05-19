package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.*;

/**
 * Parallel node is used to populate the "smart" Monte Carlo tree. Unlike the {@link LightNode} we require the use of
 * atomic variables to deal with shared access.
 */

public class HeavyNode extends AbstractNode {
    private final Double explorationConstant;
    protected HeavyNode(MiniBoard miniBoard,
                        AbstractNode parent,
                        Double explorationConstant) {
        super(miniBoard, parent, explorationConstant);
        this.explorationConstant = explorationConstant;
    }

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
    protected AbstractNode getNewNode(MiniBoard miniBoard, AbstractNode parent) {
        return new HeavyNode(miniBoard, parent, explorationConstant);
    }
}
