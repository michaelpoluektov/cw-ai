package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.MiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.DefaultMovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.BreadthFirstSearch;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXAvailableMovesScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXLocationScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.DefaultTicketPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;


/**
 * Basic MrX AI using:
 * <p>- The {@link MiniMax} location picker with the {@link MrXLocationScore} and
 * {@link MrXAvailableMovesScore} intermediate scoring functions. Looks ahead 6 steps (which represents 3-step
 * lookahead for a game with 5 detectives).</p>
 * <p>- The default move and location picker.</p>
 */
public class PruningNStep_AI implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override public String name() { return "Moriarty I - Pruning NStep"; }

    @Nonnull @Override public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        // MiniMax doesn't support a timeout at this point, but this is here in case it does later
        final Long endTimeMillis = timeoutPair.right().toMillis(timeoutPair.left())+System.currentTimeMillis();
        final IntermediateScore locationScore =
                new MrXLocationScore(constants, new BreadthFirstSearch(board.getSetup().graph));
        final IntermediateScore availableMovesScore = new MrXAvailableMovesScore(constants);
        final LocationPicker miniMax = new MiniMax(board,6, locationScore, availableMovesScore);
        final TicketPicker defaultTicketPicker = new DefaultTicketPicker(board);
        final MovePicker<LocationPicker> defaultMovePicker = new DefaultMovePicker(board, endTimeMillis, constants);
        return defaultMovePicker.pickMove(miniMax, defaultTicketPicker);
    }
}
