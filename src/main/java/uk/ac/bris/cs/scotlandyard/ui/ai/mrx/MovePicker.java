package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;

public interface MovePicker<T extends LocationPicker> {
    /**
     * @param locationPicker LocationPicker responsible for picking the best destination(s), uninfluenced by tickets
     * @param ticketPicker TicketPicker responsible for picking the best move based on the tickets used in that move
     * @return Best Move based on destination and used tickets
     * @see LocationPicker
     * @see TicketPicker
     */
    @Nonnull Move pickMove(T locationPicker, TicketPicker ticketPicker);
}
