package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractNode {

    private final MiniBoard miniBoard;
    private final AbstractNode parent;
    private final Integer roundSize;
    private final List<AbstractNode> children;
    private final NodeUCTComparator comparator;
    private final AtomicInteger plays;
    private final AtomicInteger score;

    protected AbstractNode(MiniBoard miniBoard, AbstractNode parent, NodeUCTComparator comparator) {
        this.miniBoard = miniBoard;
        this.parent = parent;
        this.roundSize = miniBoard.getSetup().rounds.size();
        this.children = new ArrayList<>();
        this.comparator = comparator;
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
        incrementPlays();
        getParent().ifPresent(AbstractNode::backPropagatePlays);
    }

    protected final void backPropagateScore(Integer round, Integer rootNodeRound) {
        if(getParent().isPresent()) {
            if(getParent().get().getMiniBoard().getMrXToMove()) incrementScore(round - rootNodeRound);
            else incrementScore(getRoundSize() + 1 - round);
            getParent().get().backPropagateScore(round, rootNodeRound);
        }
    }

    protected abstract Integer rollout(IntermediateScore... intermediateScores);

    protected void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(getMiniBoard().getWinner() == MiniBoard.winner.NONE) {
            children.addAll(getMiniBoard().getAdvancedMiniBoards().stream()
                    .map(newMiniBoard -> getNewNode(newMiniBoard, this, comparator))
                    .collect(Collectors.toSet()));
        }
    }

    protected final AbstractNode select() {
        if(isLeaf()) {
            return this;
        } else {
            return Collections.max(getChildren(), comparator).select();
        }
    }

    protected final void addChildren(ImmutableSet<Integer> destinations) {
        if(getParent().isPresent()) throw new UnsupportedOperationException("Can not add children to non root node!");
        else {
            destinations.stream()
                    .map(getMiniBoard()::advanceMrX)
                    .map(advancedMiniBoard -> getNewNode(advancedMiniBoard, this, comparator))
                    .forEach(children::add);
        }
    }

    protected abstract AbstractNode getNewNode(MiniBoard miniBoard, AbstractNode parent, NodeUCTComparator comparator);
}
