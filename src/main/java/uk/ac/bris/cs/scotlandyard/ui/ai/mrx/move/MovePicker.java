package uk.ac.bris.cs.scotlandyard.ui.ai.mrx.move;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;

public interface MovePicker {
    @Nonnull Move pickMove(LocationPicker locationPicker, TicketPicker ticketPicker);
}
