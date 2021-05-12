package uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveConverter;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DefaultMovePicker implements MovePicker<LocationPicker> {
    private final Board board;
    private final Long endTimeMillis;
    private final Double doubleThreshold;
    private final Double doubleOffset;
    private final Long timeoutOffsetMillis;
    public DefaultMovePicker(Board board, Long endTimeMillis, Toml constants) {
        this.board = board;
        this.endTimeMillis = endTimeMillis;
        this.doubleThreshold = constants.getDouble("double.threshold", 0.4);
        this.doubleOffset = constants.getDouble("double.minOffset", 0.2);
        this.timeoutOffsetMillis = constants.getLong("monteCarlo.timeoutOffsetMillis", (long) 300);
    }
    @Nonnull
    @Override
    public Move pickMove(LocationPicker locationPicker, TicketPicker ticketPicker) {
        Long simMillisTime = (endTimeMillis-System.currentTimeMillis()-300)/2;
        Pair<Long, TimeUnit> simulationTime = new Pair<>(simMillisTime, TimeUnit.MILLISECONDS);
        MoveConverter converter = new MoveConverter(board.getAvailableMoves());
        ImmutableSet<Integer> singleMoveDestinations = converter.getSingleMoveDestinations();
        System.out.println("RATING SINGLE MOVES");
        Map.Entry<Integer, Double> bestSingleEntry =
                locationPicker.getBestDestination(singleMoveDestinations, simulationTime);
        Double bestSingleValue = bestSingleEntry.getValue();
        if(bestSingleValue < doubleThreshold){
            ImmutableSet<Integer> doubleMoveDestinations = converter.getDoubleMoveDestinations();
            if(!doubleMoveDestinations.isEmpty()) {
                System.out.println("RATING DOUBLE MOVES");
                Map.Entry<Integer, Double> bestDoubleEntry =
                        locationPicker.getBestDestination(doubleMoveDestinations, simulationTime);
                Double bestDoubleValue = bestDoubleEntry.getValue();
                if(bestDoubleValue > bestSingleValue + doubleOffset || bestSingleValue == 0) {
                    ImmutableSet<Move> doubleMoves = converter.getMovesWithDestination(bestDoubleEntry.getKey());
                    return ticketPicker.getBestMoveByTickets(doubleMoves);
                }
            }
        }
        ImmutableSet<Move> singleMoves = converter.getMovesWithDestination(bestSingleEntry.getKey());
        return ticketPicker.getBestMoveByTickets(singleMoves);
    }
}
