package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.*;

/**
 * Defines an abstract node used for Monte Carlo Tree Search
 * @see AbstractMonteCarlo
 * @see RootNode
 */

public abstract class AbstractNode {

    private final MiniBoard miniBoard;
    private final AbstractNode parent;
    private final Integer roundSize;

    protected AbstractNode(MiniBoard miniBoard, AbstractNode parent) {
        this.miniBoard = miniBoard;
        this.parent = parent;
        this.roundSize = miniBoard.getSetup().rounds.size();
    }

    protected abstract Double getAverageScore();

    protected abstract Integer getPlays();

    protected final MiniBoard getMiniBoard() {
        return miniBoard;
    }

    protected final Optional<AbstractNode> getParent() {
        return Optional.ofNullable(parent);
    }

    protected abstract ImmutableSet<AbstractNode> getChildren();

    protected Integer getRound() {
        return miniBoard.getRound();
    }

    protected Integer getRoundSize() {
        return roundSize;
    }

    protected final Boolean isLeaf() {
        return getChildren().isEmpty();
    }

    protected abstract void backPropagatePlays();

    protected abstract void backPropagateScore(Integer round, Integer rootNodeRound);

    protected abstract Integer rollout();

    protected abstract void expand();
}
