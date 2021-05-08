package uk.ac.bris.cs.scotlandyard.ui.ai.ticket;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class DefaultTicketPicker implements TicketPicker {
    private final Board.TicketBoard ticketBoard;
    private final Boolean isReveal;
    private final Boolean isNextReveal;
    public DefaultTicketPicker(Board board) {
        this.ticketBoard = board.getPlayerTickets(Piece.MrX.MRX).orElseThrow();
        Boolean tempReveal;
        try {
            tempReveal = board.getSetup().rounds.get(board.getMrXTravelLog().size() - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            tempReveal = false;
        }
        this.isReveal = tempReveal;
        this.isNextReveal = board.getSetup().rounds.get(board.getMrXTravelLog().size());
    }
    @Nonnull
    @Override
    public Move getBestMove(ImmutableSet<Move> moves) {
        Set<Move> filteredList = new HashSet<>(moves);
        Comparator<Move> firstTicketComparator = new MoveFirstTicketComparator(ticketBoard);
        if(moves.stream().anyMatch(move -> move instanceof Move.SingleMove)) {
            return moves.stream()
                    .filter(move -> move instanceof Move.SingleMove)
                    .filter(move -> isReveal == Iterables.contains(move.tickets(), ScotlandYard.Ticket.SECRET))
                    .max(firstTicketComparator)
                    .orElseGet(() -> moves.stream()
                            .filter(move -> move instanceof Move.SingleMove)
                            .max(firstTicketComparator)
                            .orElseThrow());
        } else {
            return null;
        }
    }
}
