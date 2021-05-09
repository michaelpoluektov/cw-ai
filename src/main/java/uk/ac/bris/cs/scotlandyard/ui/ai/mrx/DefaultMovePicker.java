package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.Map;

public class DefaultMovePicker implements MovePicker {
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
        ImmutableSet<Move> availableMoves = board.getAvailableMoves();
        ImmutableSet<Integer> singleMoveDestinations = getSingleMoveDestinations(availableMoves);
        System.out.println("RATING SINGLE MOVES");
        Map.Entry<Integer, Double> bestSingleEntry = locationPicker.getBestDestination(singleMoveDestinations);
        Double bestSingleValue = bestSingleEntry.getValue();
        if(bestSingleValue < doubleThreshold){
            ImmutableSet<Integer> doubleMoveDestinations = getDoubleMoveDestinations(availableMoves);
            if(!doubleMoveDestinations.isEmpty()) {
                System.out.println("RATING DOUBLE MOVES");
                Map.Entry<Integer, Double> bestDoubleEntry = locationPicker.getBestDestination(doubleMoveDestinations);
                Double bestDoubleValue = bestDoubleEntry.getValue();
                if(bestDoubleValue > bestSingleValue + doubleOffset || bestSingleValue == 0) {
                    ImmutableSet<Move> doubleMoves = getDoubleMovesWithDestination(availableMoves, bestDoubleEntry.getKey());
                    return ticketPicker.getBestMove(doubleMoves);
                }
            }
        }
        ImmutableSet<Move> singleMoves = getSingleMovesWithDestination(availableMoves, bestSingleEntry.getKey());
        return ticketPicker.getBestMove(singleMoves);
    }

    private ImmutableSet<Integer> getSingleMoveDestinations(ImmutableSet<Move> moves) {
        return moves.stream()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move -> ((Move.SingleMove) move).destination)
                .distinct()
                .collect(ImmutableSet.toImmutableSet());
    }

    private ImmutableSet<Integer> getDoubleMoveDestinations(ImmutableSet<Move> moves) {
        return moves.stream()
                .filter(move -> move instanceof Move.DoubleMove)
                .map(move -> ((Move.DoubleMove) move).destination2)
                .distinct()
                .filter(destination -> !getSingleMoveDestinations(moves).contains(destination))
                .collect(ImmutableSet.toImmutableSet());
    }

    private ImmutableSet<Move> getSingleMovesWithDestination(ImmutableSet<Move> allMoves, Integer destination) {
        return allMoves.stream()
                .filter(move -> move.visit(new MoveDestinationVisitor()).equals(destination))
                .filter(move -> move instanceof Move.SingleMove)
                .collect(ImmutableSet.toImmutableSet());
    }

    private ImmutableSet<Move> getDoubleMovesWithDestination(ImmutableSet<Move> allMoves, Integer destination) {
        return allMoves.stream()
                .filter(move -> move.visit(new MoveDestinationVisitor()).equals(destination))
                .collect(ImmutableSet.toImmutableSet());
    }
}
