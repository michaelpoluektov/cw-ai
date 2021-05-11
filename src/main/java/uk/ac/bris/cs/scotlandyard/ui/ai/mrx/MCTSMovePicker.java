package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.MonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;

public class MCTSMovePicker implements MovePicker<MonteCarlo> {
    @Nonnull
    @Override
    public  Move pickMove(MonteCarlo locationPicker, TicketPicker ticketPicker) {
        return null;
    }
}
