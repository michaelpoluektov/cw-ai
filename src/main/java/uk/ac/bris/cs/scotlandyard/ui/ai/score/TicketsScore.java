package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TicketsScore extends AbstractScore {
    private final Board board;
    private final double doubleTicketsWeight = constants.getDouble("tickets.double.weight");
    private final double busTicketsWeight = constants.getDouble("tickets.bus.weight");
    private final double taxiTicketsWeight = constants.getDouble("tickets.taxi.weight");
    private final double secretTicketsWeight = constants.getDouble("tickets.secret.weight");
    private final double undergroundTicketsWeight = constants.getDouble("tickets.underground.weight");
    public TicketsScore(
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
        return null;
    }

    @Nonnull
    @Override
    public Double getDetectivesScore() {
        return null;
    }
}
