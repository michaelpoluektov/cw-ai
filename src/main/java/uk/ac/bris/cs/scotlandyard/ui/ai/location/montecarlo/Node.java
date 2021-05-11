package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Optional;
import java.util.Random;

public class Node implements Comparable<Node> {

    private final MiniBoard miniBoard;
    private Integer plays;
    private Integer score;
    private final Node parent;
    private ImmutableSet<Node> children;

    protected Node(Board board) {
        this.miniBoard = new MiniBoard(board);
        this.plays = 0;
        this.score = 0;
        this.parent = null;
        this.children = board.getAvailableMoves().stream()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move -> ((Move.SingleMove) move).destination)
                .map(miniBoard::advanceMrX)
                .map(miniBoard1 -> new Node(miniBoard1, this))
                .collect(ImmutableSet.toImmutableSet());
    }

    protected Node(MiniBoard miniBoard, Node parent) {
        this.miniBoard = miniBoard;
        this.plays = 0;
        this.score = 0;
        this.parent = parent;
        this.children = ImmutableSet.of();
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

    protected final ImmutableSet<Node> getChildren(){
        return children;
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
            this.children = miniBoard.getAdvancedMiniBoards().stream()
                    .map(value -> new Node(value, this))
                    .collect(ImmutableSet.toImmutableSet());
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
