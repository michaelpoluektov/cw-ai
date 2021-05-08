package uk.ac.bris.cs.scotlandyard.ui.ai.ticket;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;

public class DefaultTicketPicker implements TicketPicker {
    @Nonnull
    @Override
    public Move getBestMove(ImmutableSet<Move.SingleMove> moves) {
        return null;
    }
}
