package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

/**
 * @deprecated Concurrency doesn't yield any performance benefits for light playouts because the lock causes a
 * bottleneck
 */

@Deprecated public class LightPMC extends ParallelMonteCarlo {
    public LightPMC(Board board, Toml constants, IntermediateScore... intermediateScores) {
        super(board, constants, intermediateScores);
    }

    @Override
    protected Integer rolloutResult(ParallelNode node, IntermediateScore... intermediateScores) {
        return node.rollout();
    }
}
