package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MrxTicketScore implements IntermediateScore {
    private final Board.TicketBoard MrxTickets;
    private final Board board;
    private final double taxiTicketsWeight;
    private final double busTicketsWeight;
    private final double undergroundTicketsWeight;
    private final double undergroundTicketExp;
    private final double doubleTicketWeight;
    private final double overallTicketWeight;


    public MrxTicketScore(Optional<Board.TicketBoard> ticketBoard, Board board, Toml constants){
        if(ticketBoard.isEmpty()) throw new IllegalArgumentException("Piece is not in game");
        this.MrxTickets  = ticketBoard.get();
        this.board = board;
        this.taxiTicketsWeight = constants.getDouble("tickets.taxi.weight");
        this.busTicketsWeight = constants.getDouble("tickets.bus.weight");
        this.undergroundTicketsWeight = constants.getDouble("tickets.underground.weight");
        this.undergroundTicketExp = constants.getDouble("tickets.underground.exp");
        this.doubleTicketWeight = constants.getDouble("tickets.double.weight");
        this.overallTicketWeight = constants.getDouble("tickets.weight");

    }
    private Double genericTicketScore(ScotlandYard.Ticket ticket){

        int numberOfDetective = board.getPlayers().size() - 1;
        int numberOfTicket = MrxTickets.getCount(ticket);
        double score;
        double ticketWeight;
        final double scoreForTaxiBus = 1 - Math.pow((1 + 0.5 * numberOfDetective), -numberOfTicket);
        if(ticket == ScotlandYard.Ticket.BUS){
            ticketWeight = busTicketsWeight;
            score = scoreForTaxiBus;
        }
        else if(ticket == ScotlandYard.Ticket.TAXI){
            ticketWeight = taxiTicketsWeight;
            score = scoreForTaxiBus;
        }
        else{
            ticketWeight = undergroundTicketsWeight;
            score = 1 - Math.pow(undergroundTicketExp, -numberOfTicket);
        }
        return score;
    }
    private Double doubleTicketScore(){
        int numberOfDouble = MrxTickets.getCount(ScotlandYard.Ticket.DOUBLE);
        double score = 1;
        switch (numberOfDouble){
            case 0:
                score = 0.0;
            case 1:
                score = 0.5;
            case 2:
                score = 1.0;
        }
        return score;
    }


    @Nonnull
    @Override
    public Double getScore() {
        Double weightSum = doubleTicketWeight + busTicketsWeight + taxiTicketsWeight + undergroundTicketsWeight;
        Double totalScore = (doubleTicketScore() * doubleTicketWeight + genericTicketScore(ScotlandYard.Ticket.BUS) * busTicketsWeight + genericTicketScore(ScotlandYard.Ticket.TAXI) * taxiTicketsWeight + genericTicketScore(ScotlandYard.Ticket.UNDERGROUND) * undergroundTicketsWeight)/weightSum;
        return totalScore;
    }
    @Nonnull
    @Override
    public Double getWeight(){
        return overallTicketWeight;
    }




}
