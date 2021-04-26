package uk.ac.bris.cs.scotlandyard.ui.ai.score;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AvailableMovesScore extends AbstractScore {
    private final Board board;
    private final double availableMovesExp = 1.25;
    public AvailableMovesScore(
            Board board) {
        this.board = board;
    }

    @Nonnull
    @Override
    public Double getMrXScore() {
        List<Move> availableSingleMoves = new ArrayList<>();
        for(Move move : board.getAvailableMoves()) {
            if(move.getClass() == Move.DoubleMove.class) availableSingleMoves.add(move);
        }
        return 1-Math.pow(availableMovesExp, -availableSingleMoves.size());
    }

    @Nonnull
    @Override
    public Double getDetectivesScore() {
        return null;
    }
}
