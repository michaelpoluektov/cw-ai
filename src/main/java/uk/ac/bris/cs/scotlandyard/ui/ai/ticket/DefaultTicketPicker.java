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

/**
 * The default ticket picker pick the ticket in the following way:
 * <p>For single moves:</p>
 * <ul>
 * <li>If the last round was a reveal round: <ul>
 *     <li>If MrX can be a move with a secret ticket, pick that move</li>
 *     <li>Otherwise, pick the move that uses the ticket MrX has the most of</li>
 * </ul></li>
 * <li>Otherwise, if the last round wasn't a reveal round:<ul>
 *      <li>Pick the move with the ticket MrX has the most of that is not a secret ticket</li>
 *      <li>Use a secret ticket if and only if that is the only available option</li>
 * </ul></li>
 * </ul>
 * <p>Same applies to each ticket in the double moves</p>
 */
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
        Comparator<Move> firstTicketComparator = new MoveTicketComparator(ticketBoard, 0);
        if(moves.stream().anyMatch(move -> move instanceof Move.SingleMove)) {
            ImmutableSet<Move> singleMoves = moves.stream()
                    .filter(move -> move instanceof Move.SingleMove)
                    .collect(ImmutableSet.toImmutableSet());
            return singleMoves.stream()
                    .filter(move -> isReveal == Iterables.contains(move.tickets(), ScotlandYard.Ticket.SECRET))
                    .max(firstTicketComparator)
                    .orElseGet(() -> singleMoves.stream()
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
    private class MoveTicketComparator implements Comparator<Move> {
        private final Board.TicketBoard ticketBoard;
        private final int index;
        protected MoveTicketComparator(Board.TicketBoard ticketBoard, int index) {
            this.ticketBoard = ticketBoard;
            this.index = index;
        }
        @Override
        public int compare(Move o1, Move o2) {
            return ticketBoard.getCount(getTicket(o1, index)) - ticketBoard.getCount(getTicket(o2, index));
        }
    }
}
