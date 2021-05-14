package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Move;

/**
 * We instantiate this class with the set of all available moves returned from the available move function in the
 * {@link uk.ac.bris.cs.scotlandyard.model.Board} and {@link MiniBoard} classes. From the instantiation we can easily
 * access single and double move destinations allowing us to deal with them separately a {@link uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MovePicker}
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

    public ImmutableSet<Move> getMovesWithDestination(Integer destination) {
        return availableMoves.stream()
                .filter(move -> move.visit(new MoveDestinationVisitor()).equals(destination))
                .collect(ImmutableSet.toImmutableSet());
    }
}
