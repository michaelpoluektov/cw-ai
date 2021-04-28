package uk.ac.bris.cs.scotlandyard.ui.ai.score;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MrXAvailableMovesScore implements Score{
    private final Board board;
    private final Double availableMovesExp;
    public MrXAvailableMovesScore(Toml constants, Board board) {
        this.board = board;
        this.availableMovesExp = constants.getDouble("availablemoves.exp");
    }

    @Nonnull
    @Override
    public Double getScore() {
        List<Move> availableSingleMoves = new ArrayList<>();
        for(Move move : board.getAvailableMoves()) {
            if(move.getClass() == Move.DoubleMove.class) availableSingleMoves.add(move);
        }
        return 1-Math.pow(availableMovesExp, -availableSingleMoves.size());
    }
}
