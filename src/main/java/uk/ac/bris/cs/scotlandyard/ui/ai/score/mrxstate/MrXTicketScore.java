package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public class MrXTicketScore implements IntermediateScore {
    private final Board.TicketBoard mrXTickets;
    private final double taxiTicketWeight;
    private final double taxiTicketExp;
    private final double busTicketWeight;
    private final double busTicketExp;
    private final double undergroundTicketWeight;
    private final double undergroundTicketExp;
    private final double overallTicketWeight;


    public MrXTicketScore(Toml constants, Board board){
        this.taxiTicketWeight = constants.getDouble("tickets.taxi.weight");
        this.taxiTicketExp = constants.getDouble("tickets.taxi.exp");
        this.busTicketWeight = constants.getDouble("tickets.bus.weight");
        this.busTicketExp = constants.getDouble("tickets.bus.exp");
        this.undergroundTicketWeight = constants.getDouble("tickets.underground.weight");
        this.undergroundTicketExp = constants.getDouble("tickets.underground.exp");
        this.overallTicketWeight = constants.getDouble("tickets.weight");
        this.mrXTickets = board.getPlayerTickets(Piece.MrX.MRX).get();

    }
    /*private Double genericTicketScore(ScotlandYard.Ticket ticket){
        int numberOfDetective = board.getPlayers().size() - 1;
        int numberOfTicket = MrXTickets.getCount(ticket);
        double score;
        final double scoreForTaxiBus = 1 - Math.pow((1 + 0.5 * numberOfDetective), -numberOfTicket);
        if(ticket == ScotlandYard.Ticket.BUS){
            score = scoreForTaxiBus;
        }
        else if(ticket == ScotlandYard.Ticket.TAXI){
            score = scoreForTaxiBus;
        }
        else{
            score = 1 - Math.pow(undergroundTicketExp, -numberOfTicket);
        }
        return score;
    }

     */
    @Nonnull
    @Override
    public Double getScore() {
        //int numberOfDetectives = board.getPlayers().size() - 1;
        double busTicketScore = 1 - Math.pow(busTicketExp, -mrXTickets.getCount(ScotlandYard.Ticket.BUS));
        double taxiTicketScore = 1 - Math.pow(taxiTicketExp, -mrXTickets.getCount(ScotlandYard.Ticket.TAXI));
        double undergroundTicketScore = 1 - Math.pow(undergroundTicketExp, -mrXTickets.getCount(ScotlandYard.Ticket.UNDERGROUND));
        double weightSum = busTicketWeight + taxiTicketWeight + undergroundTicketWeight;
        if(weightSum == 0) throw new IllegalArgumentException("Can't divide total score by 0");
        double totalScore = busTicketScore * busTicketWeight
                + taxiTicketScore * taxiTicketWeight
                + undergroundTicketScore * undergroundTicketWeight;
        return totalScore/weightSum;
    }
    @Nonnull
    @Override
    public Double getWeight(){
        return overallTicketWeight;
    }
}