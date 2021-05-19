package uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveConverter;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.PlayoutObserver;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.TreeSimulation;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MCTSMovePicker implements MovePicker<TreeSimulation>, PlayoutObserver {
    private final MoveConverter converter;
    private final Long endTimeMillis;
    private final Double doubleThreshold;
    private final Long timeoutOffset;
    private Boolean addedDoubles;
    private Pair<Long, TimeUnit> simTime;
    public MCTSMovePicker(Board board, Long endTimeMillis, Toml constants) {
        this.converter = new MoveConverter(board.getAvailableMoves());
        this.endTimeMillis = endTimeMillis;
        this.doubleThreshold = constants.getDouble("monteCarlo.mctsDoubleThreshold", 3.0);
        this.timeoutOffset = constants.getLong("monteCarlo.timeoutOffsetMillis", (long) 300);
        this.addedDoubles = false;

    }
    @Nonnull
    @Override
    public Move pickMove(TreeSimulation locationPicker, TicketPicker ticketPicker) {
        simTime = new Pair<>(endTimeMillis - System.currentTimeMillis() - timeoutOffset, TimeUnit.MILLISECONDS);
        Map.Entry<Integer, Double> bestDestination =
                locationPicker.getBestDestination(converter.getSingleMoveDestinations(), simTime);
        return ticketPicker.getBestMoveByTickets(converter.getMovesWithDestination(bestDestination.getKey()));
    }

    /**
     * If the best single move is less than the double threshold, and we have spent 2/3's of the total time simulating,
     * then we add the double move destinations into the simulation for the final period of time.
     * @param simulations Amount of simulations ran to produce the result
     * @param bestScore Best predicted score of the children {@link AbstractNode} (representing game states)
     * @param remainingTime Remaining time (in milliseconds) until a move has to be returned
     * @param observable Reference to the observable object
     * @see TreeSimulation
     */

    @Override
    public void respondToPlayout(Integer simulations, Double bestScore, Long remainingTime, TreeSimulation observable) {
        if(!addedDoubles && remainingTime < simTime.right().toMillis(simTime.left())/3 && bestScore < doubleThreshold) {
            addedDoubles = true;
            System.out.println("BEST SINGLE SCORE IS "+bestScore+", ADDING DOUBLE MOVES");
            observable.addDestinations(converter.getDoubleMoveDestinations());
        }
    }
}
