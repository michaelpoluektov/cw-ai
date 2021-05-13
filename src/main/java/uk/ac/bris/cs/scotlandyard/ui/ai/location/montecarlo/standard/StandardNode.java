package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.standard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;

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
    public Double getAverageScore() {
        return (double) score/plays;
    }

    @Override
    public Integer getPlays() {
        return plays;
    }

    @Override
    public ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    public void backPropagatePlays() {
        plays++;
        getParent().ifPresent(AbstractNode::backPropagatePlays);
    }

    @Override
    public void backPropagateScore(Integer round, Integer rootNodeRound) {
        if(getParent().isEmpty()) score += round-getMiniBoard().getRound();
        else {
            if(getParent().get().getMiniBoard().getMrXToMove()) score += (round - rootNodeRound);
            else score += getRoundSize() - round;
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
                    .map(value -> new StandardNode(value, this))
                    .collect(Collectors.toSet()));
        }
    }
}
