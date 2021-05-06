package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import javafx.scene.Parent;
import org.checkerframework.checker.units.qual.A;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

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
        ArrayList<MCNode> tempChildren = new ArrayList<>();
        ImmutableSet<MiniBoard> advancedMiniBoards = board.getAvailableMoves().stream()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move -> ((Move.SingleMove) move).destination)
                .map(miniBoard::advanceMrX)
                .collect(ImmutableSet.toImmutableSet());
        for(MiniBoard advancedMiniBoard : advancedMiniBoards){
            tempChildren.add(new MCNode(advancedMiniBoard, this)); //This might work
        }
        this.children = Optional.of(ImmutableSet.copyOf(tempChildren));
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

    public Boolean isLeaf() {
        return children.isEmpty();
    }

    public void backPropagate(Boolean hasMrXWon) {
        this.plays ++;
        if(this.parent.isEmpty()) this.score ++;
        else {
            if(this.parent.get().getMiniBoard().getMrXToMove() == hasMrXWon) this.score ++;
            this.parent.get().backPropagate(hasMrXWon);
        }
    }

    public void rollout() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAllMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        backPropagate(rollingMiniBoard.getWinner() == MiniBoard.winner.MRX);
    }
}
