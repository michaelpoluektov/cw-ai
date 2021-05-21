package uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveConverter;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.MiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.TreeSimulation;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The default move picker picks a move by first passing all the single locations to the location picker, then
 * if the best score is below a certain threshold passing all the double locations to the same location picker and
 * comparing the results. This can theoretically be used for both location pickers currently implemented in this version
 * ({@link MiniMax} and {@link TreeSimulation}, as displayed in the Moriarty I & II agents), however it is recommended
 * to use {@link MCTSMovePicker} for optimal results with {@link TreeSimulation}.
 */
public class DefaultMovePicker implements MovePicker<LocationPicker> {
    private final Board board;
    private final Long endTimeMillis;
    private final Double doubleThreshold;
    private final Double doubleOffset;
    private final Long timeoutOffsetMillis;
    public DefaultMovePicker(Board board, Long endTimeMillis, Toml constants) {
        this.board = board;
        this.endTimeMillis = endTimeMillis;
        this.doubleThreshold = constants.getDouble("double.threshold");
        this.doubleOffset = constants.getDouble("double.minOffset");
        this.timeoutOffsetMillis = constants.getLong("timeoutOffsetMillis");
    }
    @Nonnull
    @Override
    public Move pickMove(LocationPicker locationPicker, TicketPicker ticketPicker) {
        final Long simMillisTime = (endTimeMillis-System.currentTimeMillis()-timeoutOffsetMillis)/2;
        final Pair<Long, TimeUnit> simulationTime = new Pair<>(simMillisTime, TimeUnit.MILLISECONDS);
        final MoveConverter converter = new MoveConverter(board.getAvailableMoves());
        final ImmutableSet<Integer> singleMoveDestinations = converter.getSingleMoveDestinations();
        System.out.println("RATING SINGLE MOVES");
        final Map<Integer, Double> singleMap = locationPicker.getScoredMap(singleMoveDestinations, simulationTime);
        final Map.Entry<Integer, Double> bestSingleEntry =
                Collections.max(singleMap.entrySet(), Map.Entry.comparingByValue());
        final Double bestSingleValue = bestSingleEntry.getValue();
        if(bestSingleValue < doubleThreshold){
            final ImmutableSet<Integer> doubleMoveDestinations = converter.getDoubleMoveDestinations();
            if(!doubleMoveDestinations.isEmpty()) {
                System.out.println("RATING DOUBLE MOVES");
                final Map<Integer, Double> doubleMap =
                        locationPicker.getScoredMap(doubleMoveDestinations, simulationTime);
                final Map.Entry<Integer, Double> bestDoubleEntry =
                        Collections.max(doubleMap.entrySet(), Map.Entry.comparingByValue());
                final Double bestDoubleValue = bestDoubleEntry.getValue();
                if(bestDoubleValue > bestSingleValue + doubleOffset || bestSingleValue == 0) {
                    final ImmutableSet<Move> doubleMoves = converter.getMovesWithDestination(bestDoubleEntry.getKey());
                    return ticketPicker.getBestMoveByTickets(doubleMoves);
                }
            }
        }
        final ImmutableSet<Move> singleMoves = converter.getMovesWithDestination(bestSingleEntry.getKey());
        return ticketPicker.getBestMoveByTickets(singleMoves);
    }
}
