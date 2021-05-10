package uk.ac.bris.cs.scotlandyard.ui.ai.ticket;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

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
    public Move getBestMoveByTickets(ImmutableSet<Move> moves) {
        Comparator<Move> firstTicketComparator = new MoveFirstTicketComparator(ticketBoard);
        if(moves.stream().anyMatch(move -> move instanceof Move.SingleMove)) {
            Stream<Move> singleMoves = moves.stream().filter(move -> move instanceof Move.SingleMove);
            return singleMoves
                    .filter(move -> isReveal == Iterables.contains(move.tickets(), ScotlandYard.Ticket.SECRET))
                    .max(firstTicketComparator)
                    .orElseGet(() -> singleMoves
                            .max(firstTicketComparator)
                            .orElseThrow());
        } else {
            Comparator<ScotlandYard.Ticket> ticketComparator = new TicketComparator(ticketBoard);
            ImmutableSet<Move> filteredSecretMoves = moves.stream()
                    .filter(move -> isReveal == (getTicket(move, 0) == ScotlandYard.Ticket.SECRET))
                    .filter(move -> isNextReveal == (getTicket(move, 1) == ScotlandYard.Ticket.SECRET))
                    .collect(ImmutableSet.toImmutableSet());
            return filteredSecretMoves.stream()
                    .filter(move -> getTicket(move, 1) ==
                            Collections.max(getPresentSecondTickets(filteredSecretMoves), ticketComparator))
                    .max(firstTicketComparator)
                    .orElseGet(() -> moves.stream()
                            .filter(move -> getTicket(move, 1) ==
                                    Collections.max(getPresentSecondTickets(moves), ticketComparator))
                            .max(firstTicketComparator).orElseThrow());
        }
    }

    private ImmutableSet<ScotlandYard.Ticket> getPresentSecondTickets(ImmutableSet<Move> moves) {
        return moves.stream().map(move -> getTicket(move, 1)).collect(ImmutableSet.toImmutableSet());
    }

    private ScotlandYard.Ticket getTicket(Move move, Integer index) {
        if(index > 1 || (index == 1 && move instanceof Move.SingleMove)) {
            throw new IllegalArgumentException("No ticket there!");
        }
        return Lists.newArrayList(move.tickets()).get(index);
    }
}
