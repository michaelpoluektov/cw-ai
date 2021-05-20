package uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker;

import com.google.common.collect.ImmutableSet;
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
    public MCTSMovePicker(Board board, Long endTimeMillis, Toml constants, String prefix) {
        this.converter = new MoveConverter(board.getAvailableMoves());
        this.endTimeMillis = endTimeMillis;
        this.doubleThreshold = constants.getDouble(prefix+"doubleThreshold", 5.0);
        this.timeoutOffset = constants.getLong("monteCarlo.timeoutOffsetMillis", 1000L);
        this.addedDoubles = false;

    }
    @Nonnull
    @Override
    public Move pickMove(TreeSimulation locationPicker, TicketPicker ticketPicker) {
        simTime = new Pair<>(endTimeMillis - System.currentTimeMillis() - timeoutOffset, TimeUnit.MILLISECONDS);
        ImmutableSet<Integer> singleDestinations = converter.getSingleMoveDestinations();
        Map<Integer, Double> locMap = locationPicker.getScoredMap(singleDestinations, simTime);
        Map.Entry<Integer, Double> bestSingle = locMap.entrySet().stream()
                .filter(entry -> singleDestinations.contains(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        Map.Entry<Integer, Double> bestDouble = locMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        System.out.println("Single: "
                        +bestSingle.getKey()
                        +" ["+String.format("%.2f", bestSingle.getValue())
                        +"], any: " +bestDouble.getKey()
                        +" ["+String.format("%.2f", bestDouble.getValue())
                        +"]");
        Integer bestDestination;
        if(bestSingle.getValue() * 1.6 > bestDouble.getValue()) bestDestination = bestSingle.getKey();
        else bestDestination = bestDouble.getKey();
        System.out.println("Going to "+bestDestination);
        return ticketPicker.getBestMoveByTickets(converter.getMovesWithDestination(bestDestination));
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
