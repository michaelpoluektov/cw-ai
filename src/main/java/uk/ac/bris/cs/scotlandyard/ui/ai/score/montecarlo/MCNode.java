package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Optional;
import java.util.Random;

public class MCNode {
    private final MiniBoard miniBoard;
    private Float plays;
    private Float score;
    private final MCNode parent;
    private ImmutableSet<MCNode> children;

    protected MCNode(Board board, Toml constants) {
        this.miniBoard = new MiniBoard(board, constants);
        this.plays = (float) 0;
        this.score = (float) 0;
        this.parent = null;
        this.children = board.getAvailableMoves().stream()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move -> ((Move.SingleMove) move).destination)
                .map(miniBoard::advanceMrX)
                .map(miniBoard1 -> new MCNode(miniBoard1, this))
                .collect(ImmutableSet.toImmutableSet());
    }

    protected MCNode(MiniBoard miniBoard, MCNode parent) {
        this.miniBoard = miniBoard;
        this.plays = (float) 0;
        this.score = (float) 0;
        this.parent = parent;
        this.children = ImmutableSet.of();
    }

    public Float getPlays() {
        return plays;
    }

    public Float getScore() {
        return score;
    }

    public MiniBoard getMiniBoard() {
        return miniBoard;
    }

    public Optional<MCNode> getParent() {
        return Optional.ofNullable(parent);
    }
    public ImmutableSet<MCNode> getChildren(){
        return this.children;
    }

    public Boolean isLeaf() {
        return children.isEmpty();
    }

    public void backPropagate(Boolean hasMrXWon) {
        plays++;
        if(getParent().isEmpty()) score++;
        else {
            if(parent.getMiniBoard().getMrXToMove() == hasMrXWon) score++;
            parent.backPropagate(hasMrXWon);
        }
    }

    public void rollout() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        if(!(plays == 0) && (this.getMiniBoard().getWinner() == MiniBoard.winner.NONE)) throw new UnsupportedOperationException("Can not rollout from visited node");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        backPropagate(rollingMiniBoard.getWinner() == MiniBoard.winner.MRX);
    }
<<<<<<< Updated upstream:src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai/score/montecarlo/MCNode.java
    public void populateChildren() {
=======

    protected void expandSingleChildren() {
>>>>>>> Stashed changes:src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai/location/montecarlo/MCNode.java
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(getMiniBoard().getWinner() == MiniBoard.winner.NONE){
            this.children = miniBoard.getAdvancedMiniBoards().stream()
                    .map(value -> new MCNode(value, this))
                    .collect(ImmutableSet.toImmutableSet());
        }
    }
<<<<<<< Updated upstream:src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai/score/montecarlo/MCNode.java
    public Double UTCScore(){
=======
    protected void setChildren(ImmutableSet<MCNode> children){
        this.children = children;
    }


    protected final Double getUCTScore(){
>>>>>>> Stashed changes:src/main/java/uk/ac/bris/cs/scotlandyard/ui/ai/location/montecarlo/MCNode.java
        if(getParent().isEmpty()) throw new IllegalArgumentException("Root node doesn't have a parent");
        if(this.getPlays() == 0) return Double.POSITIVE_INFINITY;
        Double exploitation = (double) this.score/this.plays;
        Double exploration = Math.sqrt(2) * Math.sqrt(Math.log(this.parent.plays)/this.plays);
        return exploitation + exploration;
    }
}
