package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class StandardNode extends AbstractNode {
    private int plays;
    private int score;
    private final Set<StandardNode> children;
    protected StandardNode(MiniBoard miniBoard,
                           StandardNode parent) {
        super(miniBoard, parent);
        this.plays = 0;
        this.score = 0;
        this.children = new HashSet<>();
    }
    @Override
    protected Double getAverageScore() {
        return (double) score/plays;
    }

    @Override
    protected Integer getPlays() {
        return plays;
    }

    @Override
    protected ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    /**
     * Propagates the plays back up the tree so long as the current node has a parent. We separated this from
     * {@link #backPropagateScore(Integer, Integer)} for consistency with the parallel implementation
     * @see ParallelNode
     */

    protected void backPropagatePlays() {
        plays++;
        getParent().ifPresent(AbstractNode::backPropagatePlays);
    }

    /**
     * The score backpropagated to MrX's child node is the number of rounds MrX loses in from the round at which he starts
     * simulation. For a detective's child node, we increment the score by the amount of rounds didn't get to live for
     * (endRound - gameEndRound).
     * We update the child's score instead of its parent due to the fact that each child nodes statistics are used
     * for its parents decision.
     * @param round Round at which the game has ended
     * @param rootNodeRound Round at which the simulations started from
     */
    @Override
    protected void backPropagateScore(Integer round, Integer rootNodeRound) {
        if(getParent().isPresent()) {
            if(getParent().get().getMiniBoard().getMrXToMove()) score += (round - rootNodeRound);
            else score += getRoundSize() + 1 - round;
            getParent().get().backPropagateScore(round, rootNodeRound);
        }
    }

    /**
     * Standard implementation of the rollout method. This randomly selects a path to the bottom of the decision tree
     * @param intermediateScore Ignored in this version of MCTS. This is considered a "light" playout.
     * @see ParallelNode for an implementation of a "heavy" playout.
     * @return Round at which the game has ended
     */
    @Override
    protected Integer rollout(IntermediateScore... intermediateScore) {
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
    protected void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(getMiniBoard().getWinner() == MiniBoard.winner.NONE) {
            children.addAll(getMiniBoard().getAdvancedMiniBoards().stream()
                    .map(value -> new StandardNode(value, this))
                    .collect(Collectors.toSet()));
        }
    }
}
