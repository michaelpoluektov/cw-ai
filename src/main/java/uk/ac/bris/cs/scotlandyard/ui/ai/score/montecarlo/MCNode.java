package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Optional;

public class MCNode {
    private final MiniBoard miniBoard;
    private Integer plays;
    private Integer score;
    private final Optional<MiniBoard> parent;
    private Optional<ImmutableSet<MiniBoard>> children;

}
