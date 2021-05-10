package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

@Deprecated class MrXTicketScore implements IntermediateScore {
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

    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard, Toml constants) {
        //int numberOfDetectives = board.getPlayers().size() - 1;
        final double busTicketScore = 1 - Math.pow(busTicketExp, -mrXTickets.getCount(ScotlandYard.Ticket.BUS));
        final double taxiTicketScore = 1 - Math.pow(taxiTicketExp, -mrXTickets.getCount(ScotlandYard.Ticket.TAXI));
        final double undergroundTicketScore =
                1 - Math.pow(undergroundTicketExp, -mrXTickets.getCount(ScotlandYard.Ticket.UNDERGROUND));
        final double weightSum = busTicketWeight + taxiTicketWeight + undergroundTicketWeight;
        if(weightSum == 0) throw new IllegalArgumentException("Can't divide total score by 0");
        final double totalScore = busTicketScore * busTicketWeight
                + taxiTicketScore * taxiTicketWeight
                + undergroundTicketScore * undergroundTicketWeight;
        return totalScore/weightSum;
    }
    @Nonnull
    @Override
    public Double getWeight(Toml constants){
        return overallTicketWeight;
    }
}