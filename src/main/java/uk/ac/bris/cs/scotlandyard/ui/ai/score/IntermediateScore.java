package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import javax.annotation.Nonnull;

public interface IntermediateScore {

    /**
     * @param miniBoard MiniBoard to score
     * @return Heuristic score evaluating the GameState from the agent's perspective, if the scoring function's
     * constants are to be improved with reinforcement learning, values between bounded from 0 (least favorable) to 1
     * (move favorable) are preferred.
     */
    @Nonnull Double getScore(MiniBoard miniBoard);

    /**
     * @return The relative weight of the scoring function
     */
    @Nonnull Double getWeight();
}
