package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

public class HeavyPMC extends ParallelMonteCarlo{
    public HeavyPMC(Board board, Toml constants, IntermediateScore... intermediateScores) {
        super(board, constants, intermediateScores);
    }

    @Override
    protected Integer rolloutResult(ParallelNode node, IntermediateScore... intermediateScores) {
        return node.heavyRollout(intermediateScores);
    }
}
