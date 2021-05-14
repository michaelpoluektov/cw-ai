package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel.ParallelNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.standard.StandardNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.*;

/**
 * This class is implemented by the {@link StandardNode} and the {@link ParallelNode}. Certain attributes and
 * methods such as parent and backPropagate are the same in both implementations hence they are declared or initialised
 * here rather than being declared abstract. Methods which require specific implementations are declared abstract.
 */

public abstract class AbstractNode {

    private final MiniBoard miniBoard;
    private final AbstractNode parent;
    private final Integer roundSize;

    public AbstractNode(MiniBoard miniBoard, AbstractNode parent) {
        this.miniBoard = miniBoard;
        this.parent = parent;
        this.roundSize = miniBoard.getSetup().rounds.size();
    }

    public abstract Double getAverageScore();

    public abstract Integer getPlays();

    public final MiniBoard getMiniBoard() {
        return miniBoard;
    }

    public final Optional<AbstractNode> getParent() {
        return Optional.ofNullable(parent);
    }

    public abstract ImmutableSet<AbstractNode> getChildren();

    public Integer getRound() {
        return miniBoard.getRound();
    }

    public Integer getRoundSize() {
        return roundSize;
    }

    public final Boolean isLeaf() {
        return getChildren().isEmpty();
    }

    public abstract void backPropagatePlays();

    public abstract void backPropagateScore(Integer round, Integer rootNodeRound);

    public abstract Integer rollout(IntermediateScore... intermediateScores);

    public abstract void expand();
}
