package uk.ac.bris.cs.scotlandyard.ui.ai.ticket;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;

public interface TicketPicker {
    /**
     * @param moves ImmutableSet of all available moves, usually with pre-filtered locations
     * @return Best Move based on Tickets
     */
    @Nonnull Move getBestMoveByTickets(ImmutableSet<Move> moves);
}
