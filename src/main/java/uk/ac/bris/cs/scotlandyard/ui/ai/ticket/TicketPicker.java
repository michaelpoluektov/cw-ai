package uk.ac.bris.cs.scotlandyard.ui.ai.ticket;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;

public interface TicketPicker {
    @Nonnull Move getBestMove(ImmutableSet<Move.SingleMove> moves);
    // @param: ImmutableSet of all possible moves with a given destination
    //
    // Return: Best move based on tickets
}
