package uk.ac.bris.cs.scotlandyard.ui.ai.ticket;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.Comparator;

public class TicketComparator implements Comparator<ScotlandYard.Ticket> {
    private final Board.TicketBoard ticketBoard;
    protected TicketComparator(Board.TicketBoard ticketBoard) {
        this.ticketBoard = ticketBoard;
    }
    @Override
    public int compare(ScotlandYard.Ticket o1, ScotlandYard.Ticket o2) {
        return ticketBoard.getCount(o1)- ticketBoard.getCount(o2);
    }
}
