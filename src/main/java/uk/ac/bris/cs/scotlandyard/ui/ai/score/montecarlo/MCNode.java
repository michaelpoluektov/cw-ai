package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Optional;

public class MCNode {
    private final MiniBoard miniBoard;
    private Integer plays;
    private Integer score;
    private final Optional<MCNode> parent;
    private Optional<ImmutableSet<MCNode>> children;

    protected MCNode(Board board, Toml constants) {
        this.miniBoard = new MiniBoard(board, constants);
        this.plays = 0;
        this.score = 0;
        this.parent = Optional.empty();
        //INITIALISE CHILDREN
    }

    protected MCNode(MiniBoard miniBoard, MCNode parent) {
        this.miniBoard = miniBoard;
        this.plays = 0;
        this.score = 0;
        this.parent = Optional.of(parent);
        this.children = Optional.empty();
    }

    public Integer getPlays() {
        return plays;
    }

    public Integer getScore() {
        return score;
    }

    public MiniBoard getMiniBoard() {
        return miniBoard;
    }

    public void backPropagate(Boolean hasMrXWon) {
        this.plays ++;
        if(this.parent.isEmpty()) this.score ++;
        else {
            if(this.parent.get().getMiniBoard().getMrXToMove() == hasMrXWon) this.score ++;
            this.parent.get().backPropagate(hasMrXWon);
        }
    }
}
