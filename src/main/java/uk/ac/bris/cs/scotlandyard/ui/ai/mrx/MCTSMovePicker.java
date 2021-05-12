package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.MonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.PlayoutObserver;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;

public class MCTSMovePicker implements MovePicker<MonteCarlo>, PlayoutObserver {
    private final Board board;
    private final Double doubleThreshold;
    private final Double doubleOffset;
    private final Long timeoutOffset;
    protected MCTSMovePicker(Board board, Toml constants) {
        this.board = board;
        this.doubleThreshold = constants.getDouble("double.threshold");
        this.doubleOffset = constants.getDouble("double.minOffset");
        this.timeoutOffset = constants.getLong("montecarlo.timeoutOffsetMillis");

    }
    @Nonnull
    @Override
    public Move pickMove(MonteCarlo locationPicker, TicketPicker ticketPicker) {
        return null;
    }

    @Override
    public void respondToPlayout(Integer simulations, Double bestScore, Long remainingTime) {

    }
}
