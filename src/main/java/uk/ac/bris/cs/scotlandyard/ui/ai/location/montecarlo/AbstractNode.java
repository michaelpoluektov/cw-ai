package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Class representing an abstract node for the Monte Carlo Tree Search algorithm. Currently, only rollout and
 * backpropagation phases are thread safe, so a lock must be used to manage access to the selection and expansion
 * stages. We are backpropagating the plays first in order to avoid multiple threads selecting the same node.
 *
 * A concrete subclass must implement the {@link #rollout(IntermediateScore...)} method, which is used to rank a given
 * node in the tree, as well as the {@link #getNewNode(MiniBoard, AbstractNode)}, returning a new
 * instance of that concrete node.
 */

public abstract class AbstractNode {

    private final MiniBoard miniBoard;
    private final AbstractNode parent;
    private final Integer roundSize;
    private final List<AbstractNode> children;
    private final AtomicInteger plays;
    private final AtomicInteger score;

    protected AbstractNode(MiniBoard miniBoard, AbstractNode parent) {
        this.miniBoard = miniBoard;
        this.parent = parent;
        this.roundSize = miniBoard.getSetup().rounds.size();
        this.children = new ArrayList<>();
        this.plays = new AtomicInteger();
        this.score = new AtomicInteger();
    }

    protected final Double getAverageScore() {
            return score.doubleValue()/plays.doubleValue();
    }

    protected final Integer getPlays() {
        return plays.get();
    }

    protected final Integer getScore() {
        return score.get();
    }

    protected final MiniBoard getMiniBoard() {
        return miniBoard;
    }

    protected final Optional<AbstractNode> getParent() {
        return Optional.ofNullable(parent);
    }

    protected final ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    protected final Integer getRound() {
        return miniBoard.getRound();
    }

    protected final Integer getRoundSize() {
        return roundSize;
    }

    protected final Boolean isLeaf() {
        return getChildren().isEmpty();
    }

    /**
     * Thread safe way to increment the plays variable by 1
     */

    private void incrementPlays() {
        while(true) {
            int currentPlays = getPlays();
            int incrementedPlays = getPlays() + 1;
            if(plays.compareAndSet(currentPlays, incrementedPlays)) {
                return;
            }
        }
    }

    /**
     * Thread safe way to increment the score variable by a given value
     * @param increment The amount we want to increase an atomic variable by.
     */

    private void incrementScore(Integer increment) {
        while(true) {
            int currentScore = getScore();
            int incrementedScore = getScore() + increment;
            if(score.compareAndSet(currentScore, incrementedScore)) {
                return;
            }
        }
    }

    protected void backPropagatePlays() {
        // This actually doesn't need to be atomic (yet)
        incrementPlays();
        getParent().ifPresent(AbstractNode::backPropagatePlays);
    }

    // The root node's score is never modified or accessed
    protected final void backPropagateScore(Integer round, Integer rootNodeRound) {
        if(getParent().isPresent()) {
            if(getParent().get().getMiniBoard().getMrXToMove()) incrementScore(round - rootNodeRound);
            else incrementScore(getRoundSize() + 1 - round);
            getParent().get().backPropagateScore(round, rootNodeRound);
        }
    }

    /**
     * The rollout result is meant to be a prediction over how many total rounds is MrX expected to survive for. This
     * prediction can either be made by a playout sequence (random -- labeled "light" playout, or calculated -- labeled
     * "heavy playout") or directly by a heuristic function.
     * @param intermediateScores Heuristic functions to use in the rollout process, can be empty
     * @return The round at which MrX is expected to be captured. 25 if MrX is expected to win.
     */
    protected abstract Integer rollout(IntermediateScore... intermediateScores);


    /**
     * Not thread safe, access must be managed.
     *
     * <p>Expands a leaf node with child nodes, representing the possible game states after a move is executed. Game states
     * obtained by the {@link MiniBoard#getAdvancedMiniBoards()} method.</p>
     */
    protected void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(getMiniBoard().getWinner() == MiniBoard.winner.NONE) {
            children.addAll(getMiniBoard().getAdvancedMiniBoards().stream()
                    .map(newMiniBoard -> getNewNode(newMiniBoard, this))
                    .collect(Collectors.toSet()));
        }
    }

    /**
     * Externally add children to root node. Used to initialise children with only available moves (including tickets)
     * and externally add children representing double moves.
     *
     * <p>This is separate from {@link #expand()} because a {@link MiniBoard} can not access double move destinations.
     * {@link #expand()} would also potentially add game states that can not be reached due to insufficient tickets,
     * which is unacceptable for the direct children of the root node since otherwise we could get our AI to output an
     * illegal move.</p>
     * @param destinations destinations of the moves, corresponding to MrX's location in the added nodes.
     */
    protected final void addChildren(ImmutableSet<Integer> destinations) {
        if(getParent().isPresent()) throw new UnsupportedOperationException("Can not add children to non root node!");
        else {
            destinations.stream()
                    .map(getMiniBoard()::advanceMrX)
                    .map(advancedMiniBoard -> getNewNode(advancedMiniBoard, this))
                    .forEach(children::add);
        }
    }

    /**
     * @param miniBoard MiniBoard wrapped by node
     * @param parent Parent node
     * @return New instance of the concrete implementation.
     */
    protected abstract AbstractNode getNewNode(MiniBoard miniBoard, AbstractNode parent);
}
