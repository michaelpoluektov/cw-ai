package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.MonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.DefaultTicketPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class DefaultMonteCarlo implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override
    public String name() {
        return "Default MCTS (D)";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        Pair<Long, TimeUnit> halfTimeout = new Pair<>(timeoutPair.left()/2, timeoutPair.right());
        LocationPicker monteCarlo = new MonteCarlo(board, halfTimeout);
        TicketPicker defaultTicketPicker = new DefaultTicketPicker(board);
        MovePicker<LocationPicker> defaultMovePicker = new DefaultMovePicker(board, constants);
        return defaultMovePicker.pickMove(monteCarlo, defaultTicketPicker);
    }
}
