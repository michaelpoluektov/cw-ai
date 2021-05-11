package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Node implements Comparable<Node> {

    private final MiniBoard miniBoard;
    private Integer plays;
    private Integer score;
    private final Node parent;
    private final Set<Node> children;

    protected Node(MiniBoard miniBoard, Node parent) {
        this.miniBoard = miniBoard;
        this.plays = 0;
        this.score = 0;
        this.parent = parent;
        this.children = new HashSet<>();
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

    protected final Boolean isLeaf() {
        return children.isEmpty();
    }

    protected void backPropagate(Boolean hasMrXWon) {
        plays++;
        if(getParent().isEmpty()) score++;
        else {
            if(parent.getMiniBoard().getMrXToMove() == hasMrXWon) score++;
            parent.backPropagate(hasMrXWon);
        }
    }

    protected Boolean rollout() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        return rollingMiniBoard.getWinner() == MiniBoard.winner.MRX;
    }

    protected void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(miniBoard.getWinner() == MiniBoard.winner.NONE) {
            children.addAll(miniBoard.getAdvancedMiniBoards().stream()
                    .map(value -> new Node(value, this))
                    .collect(Collectors.toSet()));
        }
    }

    protected final Double getUCTScore(){
        if(getParent().isEmpty()) throw new IllegalArgumentException("Root node doesn't have a parent");
        Double exploitation = getAverageScore();
        Double exploration = Math.sqrt(2) * Math.sqrt(Math.log(parent.getPlays())/plays);
        return exploitation + exploration;
    }

    @Override
    public int compareTo(Node node) {
        return Double.compare(getUCTScore(), node.getUCTScore());
    }
}
