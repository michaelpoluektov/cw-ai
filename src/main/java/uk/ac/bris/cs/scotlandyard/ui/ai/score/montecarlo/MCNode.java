package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Optional;

public class MCNode {
    private final MiniBoard miniBoard;
    private Integer plays;
    private Integer score;
    private final Optional<MCNode> parent;
    private Optional<ImmutableSet<MCNode>> children;

    protected MCNode(MiniBoard miniBoard, MCNode parent) {
        this.miniBoard = miniBoard;
        this.plays = 0;
        this.score = 0;
        this.parent = Optional.of(parent);
        this.children = Optional.empty();
    }
}
