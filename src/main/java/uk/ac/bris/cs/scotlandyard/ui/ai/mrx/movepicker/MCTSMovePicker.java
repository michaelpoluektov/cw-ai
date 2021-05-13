package uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveConverter;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractMonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.standard.StandardMonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.PlayoutObserver;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MCTSMovePicker implements MovePicker<AbstractMonteCarlo>, PlayoutObserver {
    private final MoveConverter converter;
    private final Long endTimeMillis;
    private final Double doubleThreshold;
    private final Double doubleOffset;
    private final Long timeoutOffset;
    private Boolean addedDoubles;
    private Pair<Long, TimeUnit> simTime;
    public MCTSMovePicker(Board board, Long endTimeMillis, Toml constants) {
        this.converter = new MoveConverter(board.getAvailableMoves());
        this.endTimeMillis = endTimeMillis;
        this.doubleThreshold = constants.getDouble("double.threshold", 0.4);
        this.doubleOffset = constants.getDouble("double.minOffset", 0.2);
        this.timeoutOffset = constants.getLong("monteCarlo.timeoutOffsetMillis", (long) 300);
        this.addedDoubles = false;

    }
    @Nonnull
    @Override
    public Move pickMove(AbstractMonteCarlo locationPicker, TicketPicker ticketPicker) {
        System.out.println("RATING SINGLE MOVES ONLY");
        simTime = new Pair<>(endTimeMillis - System.currentTimeMillis() - timeoutOffset, TimeUnit.MILLISECONDS);
        Map.Entry<Integer, Double> bestDestination =
                locationPicker.getBestDestination(converter.getSingleMoveDestinations(), simTime);
        return ticketPicker.getBestMoveByTickets(converter.getMovesWithDestination(bestDestination.getKey()));
    }

    @Override
    public void respondToPlayout(Integer simulations, Double bestScore, Long remainingTime, AbstractMonteCarlo observable) {
        if(!addedDoubles && remainingTime < simTime.right().toMillis(simTime.left())/3 && bestScore < doubleThreshold) {
            addedDoubles = true;
            System.out.println("BEST SINGLE SCORE IS "+bestScore+", ADDING DOUBLE MOVES");
            observable.addDestinations(converter.getDoubleMoveDestinations());
        }
    }
}
