package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.MiniMax;
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
public class PruningNStep implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override public String name() { return "Default pruning N-Step (D)"; }

    @Nonnull @Override public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        IntermediateScore locationScore = new MrXLocationScore(constants);
        IntermediateScore availableMovesScore = new MrXAvailableMovesScore(constants);
        LocationPicker miniMax = new MiniMax(board, constants, 6, locationScore, availableMovesScore);
        TicketPicker defaultTicketPicker = new DefaultTicketPicker(board);
        MovePicker defaultMovePicker = new DefaultMovePicker(board, constants);
        return defaultMovePicker.pickMove(miniMax, defaultTicketPicker);
    }
}
