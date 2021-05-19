package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.TreeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.DefaultMovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.DefaultTicketPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class DefaultMCTS_AI implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override
    public String name() {
        return "Moriarty II - MCTS with light rollout, default move picker";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        final Long endTimeMillis = timeoutPair.right().toMillis(timeoutPair.left())+System.currentTimeMillis();
        final LocationPicker standardMonteCarlo = TreeFactory.newLightTree(board, constants);
        final TicketPicker defaultTicketPicker = new DefaultTicketPicker(board);
        final MovePicker<LocationPicker> defaultMovePicker = new DefaultMovePicker(board, endTimeMillis, constants);
        return defaultMovePicker.pickMove(standardMonteCarlo, defaultTicketPicker);
    }
}
