package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    public Double getAverageScore() {
        return score.doubleValue()/plays.doubleValue();
    }

    @Override
    public Integer getPlays() {
        return plays.get();
    }

    public Integer getScore() {
        return score.get();
    }

    @Override
    public ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    private void incrementPlays() {
        while(true) {
            int currentPlays = getPlays();
            int incrementedPlays = getPlays() + 1;
            if(plays.compareAndSet(currentPlays, incrementedPlays)) {
                return;
            }
        }
    }

    private void incrementScore(Integer increment) {
        while(true) {
            int currentScore = getScore();
            int incrementedScore = getScore() + increment;
            if(score.compareAndSet(currentScore, incrementedScore)) {
                return;
            }
        }
    }

    public void backPropagatePlays() {
        incrementPlays();
        getParent().ifPresent(AbstractNode::backPropagatePlays);
    }

    @Override
    public void backPropagateScore(Integer round, Integer rootNodeRound) {
        if(getParent().isEmpty()) incrementScore(round-getMiniBoard().getRound());
        else {
            if(getParent().get().getMiniBoard().getMrXToMove()) incrementScore(round - rootNodeRound);
            else incrementScore(getRoundSize() - round);
            getParent().get().backPropagateScore(round, rootNodeRound);
        }
    }

    @Override
    public Integer rollout() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        return rollingMiniBoard.getRound();
    }

    @Override
    public void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(getMiniBoard().getWinner() == MiniBoard.winner.NONE) {
            children.addAll(getMiniBoard().getAdvancedMiniBoards().stream()
                    .map(value -> new ParallelNode(value, this))
                    .collect(Collectors.toSet()));
        }
    }
}
