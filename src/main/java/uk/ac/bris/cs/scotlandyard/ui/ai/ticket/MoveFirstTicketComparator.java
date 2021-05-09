package uk.ac.bris.cs.scotlandyard.ui.ai.ticket;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.Comparator;

public class MoveFirstTicketComparator implements Comparator<Move> {
    private final Board.TicketBoard ticketBoard;
    protected MoveFirstTicketComparator(Board.TicketBoard ticketBoard) {
        this.ticketBoard = ticketBoard;
    }
    @Override
    public int compare(Move o1, Move o2) {
        return ticketBoard.getCount(firstTicket(o1)) - ticketBoard.getCount(firstTicket(o2));
    }

    private ScotlandYard.Ticket firstTicket(Move move) {
        return move.tickets().iterator().next();
    }
}
