package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Parallel node is used to populate the "smart" Monte Carlo tree. Unlike the {@link StandardNode} we require the use of
 * atomic variables to deal with shared access.
 */

public class ParallelNode extends AbstractNode {
    private final AtomicInteger plays;
    private final AtomicInteger score;
    private final Set<ParallelNode> children;
    protected ParallelNode(MiniBoard miniBoard,
                           ParallelNode parent) {
        super(miniBoard, parent);
        this.plays = new AtomicInteger();
        this.score = new AtomicInteger();
        this.children = Collections.synchronizedSet(new HashSet<>());
    }
    @Override
    protected Double getAverageScore() {
        return score.doubleValue()/plays.doubleValue();
    }

    @Override
    protected Integer getPlays() {
        return plays.get();
    }

    protected Integer getScore() {
        return score.get();
    }

    @Override
    protected ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
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
        incrementPlays();
        getParent().ifPresent(AbstractNode::backPropagatePlays);
    }


    @Override
    protected void backPropagateScore(Integer round, Integer rootNodeRound) {
        if(getParent().isPresent()) {
            if(getParent().get().getMiniBoard().getMrXToMove()) incrementScore(round - rootNodeRound);
            else incrementScore(getRoundSize() + 1 - round);
            getParent().get().backPropagateScore(round, rootNodeRound);
        }
    }

    /**
     * Standard random rollout.
     * @throws UnsupportedOperationException If the node already has children.
     * @return The round of the termination node, return the final round + 1 if MrX win
     */
    @Override
    protected Integer rollout() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        if(rollingMiniBoard.getWinner() == MiniBoard.winner.MRX) return getRoundSize()+1;
        else return rollingMiniBoard.getRound();
    }

    /**
     * Unlike {@link #rollout()} the rollout procedure is not random and instead uses a 1 step lookahead AI to pick
     * the path we rollout down the tree.
     * @throws UnsupportedOperationException If the node already has children.
     * @return The round of the termination node, return the final round + 1 if MrX win
     */
    protected Integer heavyRollout(IntermediateScore... intermediateScores) {
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

    /**
     * NOT THREAD SAFE, ACCESS MUST BE MANAGED
     * Populates the children of the node with the possible advanced game states
     * @throws UnsupportedOperationException If children already populated
     */
    @Override
    protected void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(getMiniBoard().getWinner() == MiniBoard.winner.NONE) {
            children.addAll(getMiniBoard().getAdvancedMiniBoards().stream()
                    .map(value -> new ParallelNode(value, this))
                    .collect(Collectors.toSet()));
        }
    }
}
