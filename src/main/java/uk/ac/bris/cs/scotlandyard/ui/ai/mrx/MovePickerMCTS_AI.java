package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.TreeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.TreeSimulation;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.DefaultTicketPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * AI agent using the default "light" version of MCTS and the corresponding move picker.
 * @see TreeSimulation
 */
public class MovePickerMCTS_AI implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override
    public String name() {
        return "Moriarty III - MCTS with light rollout, enhanced move picker";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        final Integer threadNumber = Runtime.getRuntime().availableProcessors();
        final Long endTimeMillis = timeoutPair.right().toMillis(timeoutPair.left())+System.currentTimeMillis();
        final TreeSimulation monteCarlo = TreeFactory.newLightTree(board, constants, threadNumber);
        final TicketPicker defaultTicketPicker = new DefaultTicketPicker(board);
        final String prefix = "monteCarlo.light.";
        final MCTSMovePicker monteCarloMovePicker = new MCTSMovePicker(board, endTimeMillis, constants, prefix);
        monteCarlo.addPlayoutObserver(monteCarloMovePicker);
        return monteCarloMovePicker.pickMove(monteCarlo, defaultTicketPicker);
    }
}
