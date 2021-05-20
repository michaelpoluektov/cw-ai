package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MovePicker;

/**
 * Provides simple a simple conversion between a set of available moves and their destinations.
 */

public class MoveConverter {
    private final ImmutableSet<Move> availableMoves;
    public MoveConverter(ImmutableSet<Move> availableMoves) {
        this.availableMoves = availableMoves;
    }

    public ImmutableSet<Integer> getSingleMoveDestinations() {
        return availableMoves.stream()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move -> ((Move.SingleMove) move).destination)
                .distinct()
                .collect(ImmutableSet.toImmutableSet());
    }

    public ImmutableSet<Integer> getDoubleMoveDestinations() {
        return availableMoves.stream()
                .filter(move -> move instanceof Move.DoubleMove)
                .map(move -> ((Move.DoubleMove) move).destination2)
                .distinct()
                .filter(destination -> !getSingleMoveDestinations().contains(destination))
                .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Does not automatically filter out double moves in case a single move with the provided destination exists. 
     */
    public ImmutableSet<Move> getMovesWithDestination(Integer destination) {
        return availableMoves.stream()
                .filter(move -> move.visit(new MoveDestinationVisitor()).equals(destination))
                .collect(ImmutableSet.toImmutableSet());
    }
}
