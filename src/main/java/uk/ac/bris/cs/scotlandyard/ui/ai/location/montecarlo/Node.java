package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.*;
import java.util.stream.Collectors;

public class Node {

    private final MiniBoard miniBoard;
    private Integer plays;
    private Integer score;
    private final Node parent;
    private final Set<Node> children;
    private final Integer roundSize;

    protected Node(MiniBoard miniBoard, Node parent) {
        this.miniBoard = miniBoard;
        this.plays = 0;
        this.score = 0;
        this.parent = parent;
        this.children = new HashSet<>();
        this.roundSize = miniBoard.getSetup().rounds.size();
    }

    protected final Double getAverageScore() {
        return (double) score/plays;
    }

    protected final Integer getPlays() {
        return plays;
    }

    protected final MiniBoard getMiniBoard() {
        return miniBoard;
    }

    protected final Optional<Node> getParent() {
        return Optional.ofNullable(parent);
    }

    protected ImmutableSet<Node> getChildren(){
        return ImmutableSet.copyOf(children);
    }

    protected Integer getRound() {
        return miniBoard.getRound();
    }

    protected final Boolean isLeaf() {
        return children.isEmpty();
    }

    protected void backPropagate(Integer round, Integer rootNodeRound) {
        plays++;
        if(getParent().isEmpty()) score += round-getMiniBoard().getRound();
        else {
            if(parent.getMiniBoard().getMrXToMove()) score += (round - rootNodeRound);
            else score += roundSize - round;
            parent.backPropagate(round, rootNodeRound);
        }
    }

    protected Integer rollout() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        return rollingMiniBoard.getRound();
    }

    protected void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(miniBoard.getWinner() == MiniBoard.winner.NONE) {
            children.addAll(miniBoard.getAdvancedMiniBoards().stream()
                    .map(value -> new Node(value, this))
                    .collect(Collectors.toSet()));
        }
    }
}
