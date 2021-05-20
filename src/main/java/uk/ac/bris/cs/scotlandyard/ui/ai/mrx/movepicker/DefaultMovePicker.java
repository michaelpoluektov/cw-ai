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
import java.util.Collections;
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
        this.doubleThreshold = constants.getDouble("double.threshold");
        this.doubleOffset = constants.getDouble("double.minOffset");
        this.timeoutOffsetMillis = constants.getLong("timeoutOffsetMillis");
    }
    @Nonnull
    @Override
    public Move pickMove(LocationPicker locationPicker, TicketPicker ticketPicker) {
        Long simMillisTime = (endTimeMillis-System.currentTimeMillis()-timeoutOffsetMillis)/2;
        Pair<Long, TimeUnit> simulationTime = new Pair<>(simMillisTime, TimeUnit.MILLISECONDS);
        MoveConverter converter = new MoveConverter(board.getAvailableMoves());
        ImmutableSet<Integer> singleMoveDestinations = converter.getSingleMoveDestinations();
        System.out.println("RATING SINGLE MOVES");
        Map<Integer, Double> singleMap = locationPicker.getScoredMap(singleMoveDestinations, simulationTime);
        Map.Entry<Integer, Double> bestSingleEntry =
                Collections.max(singleMap.entrySet(), Map.Entry.comparingByValue());
        Double bestSingleValue = bestSingleEntry.getValue();
        if(bestSingleValue < doubleThreshold){
            ImmutableSet<Integer> doubleMoveDestinations = converter.getDoubleMoveDestinations();
            if(!doubleMoveDestinations.isEmpty()) {
                System.out.println("RATING DOUBLE MOVES");
                Map<Integer, Double> doubleMap =
                        locationPicker.getScoredMap(doubleMoveDestinations, simulationTime);
                Map.Entry<Integer, Double> bestDoubleEntry =
                        Collections.max(doubleMap.entrySet(), Map.Entry.comparingByValue());
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
