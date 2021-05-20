package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.annotations.Beta;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.TreeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.TreeSimulation;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.BreadthFirstSearch;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXLiteLocationScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.DefaultTicketPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * Monte Carlo Tree search with heavy rollouts. We are still unsure if this produces better results than {@link MovePickerMCTS_AI}.
 * Quoting from this <a href=https://en.wikipedia.org/wiki/Monte_Carlo_tree_search>Wikipedia</a> article:
 * "Paradoxically, playing suboptimally in simulations sometimes makes a Monte Carlo tree search program play stronger overall."
 * More information could potentially be found in this quoted
 * <a href=/publication/220834574_Investigating_the_Effects_of_Playout_Strength_in_Monte-Carlo_Go>research article</a>,
 * however it must be stated that we didn't actually manage to get access to it.
 */

@Beta
public class HeavyRolloutMCTS_AI implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override
    public String name() {
        return "Moriarty IV - MCTS with heavy rollout, multithreading";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        final Integer threadNumber = Runtime.getRuntime().availableProcessors();
        final Long endTimeMillis = timeoutPair.right().toMillis(timeoutPair.left())+System.currentTimeMillis();
        final IntermediateScore locationScore = new MrXLiteLocationScore(new BreadthFirstSearch(board.getSetup().graph));
        final TreeSimulation monteCarlo = TreeFactory.newHeavyTree(board, constants, threadNumber, locationScore);
        final TicketPicker defaultTicketPicker = new DefaultTicketPicker(board);
        final String prefix = "monteCarlo.heavy.";
        final MCTSMovePicker monteCarloMovePicker = new MCTSMovePicker(board, endTimeMillis, constants, prefix);
        monteCarlo.addPlayoutObserver(monteCarloMovePicker);
        return monteCarloMovePicker.pickMove(monteCarlo, defaultTicketPicker);
    }
}