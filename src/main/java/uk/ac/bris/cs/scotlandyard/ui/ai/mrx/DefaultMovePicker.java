package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveConverter;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.Map;

public class DefaultMovePicker implements MovePicker<LocationPicker> {
    private final Board board;
    private final Double doubleThreshold;
    private final Double doubleOffset;
    protected DefaultMovePicker(Board board, Toml constants) {
        this.board = board;
        this.doubleThreshold = constants.getDouble("double.threshold");
        this.doubleOffset = constants.getDouble("double.minOffset");
    }
    @Nonnull
    @Override
    public Move pickMove(LocationPicker locationPicker, TicketPicker ticketPicker) {
        MoveConverter converter = new MoveConverter(board.getAvailableMoves());
        ImmutableSet<Integer> singleMoveDestinations = converter.getSingleMoveDestinations();
        System.out.println("RATING SINGLE MOVES");
        Map.Entry<Integer, Double> bestSingleEntry = locationPicker.getBestDestination(singleMoveDestinations);
        Double bestSingleValue = bestSingleEntry.getValue();
        if(bestSingleValue < doubleThreshold){
            ImmutableSet<Integer> doubleMoveDestinations = converter.getDoubleMoveDestinations();
            if(!doubleMoveDestinations.isEmpty()) {
                System.out.println("RATING DOUBLE MOVES");
                Map.Entry<Integer, Double> bestDoubleEntry = locationPicker.getBestDestination(doubleMoveDestinations);
                Double bestDoubleValue = bestDoubleEntry.getValue();
                if(bestDoubleValue > bestSingleValue + doubleOffset || bestSingleValue == 0) {
                    ImmutableSet<Move> doubleMoves = converter.getDoubleMovesWithDestination(bestDoubleEntry.getKey());
                    return ticketPicker.getBestMoveByTickets(doubleMoves);
                }
            }
        }
        ImmutableSet<Move> singleMoves = converter.getSingleMovesWithDestination(bestSingleEntry.getKey());
        return ticketPicker.getBestMoveByTickets(singleMoves);
    }
}
