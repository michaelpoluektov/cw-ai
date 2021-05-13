package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel.ParallelMonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXLiteLocationScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.DefaultTicketPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class SmartMonteCarlo implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override
    public String name() {
        return "Parallel MCTS - (D)";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        final Long endTimeMillis = timeoutPair.right().toMillis(timeoutPair.left())+System.currentTimeMillis();
        final IntermediateScore locationScore = new MrXLiteLocationScore(new Dijkstra(board.getSetup().graph));
        final ParallelMonteCarlo monteCarlo = new ParallelMonteCarlo(board, constants, locationScore);
        final TicketPicker defaultTicketPicker = new DefaultTicketPicker(board);
        final MCTSMovePicker monteCarloMovePicker = new MCTSMovePicker(board, endTimeMillis, constants);
        monteCarlo.addPlayoutObserver(monteCarloMovePicker);
        return monteCarloMovePicker.pickMove(monteCarlo, defaultTicketPicker);
    }
}