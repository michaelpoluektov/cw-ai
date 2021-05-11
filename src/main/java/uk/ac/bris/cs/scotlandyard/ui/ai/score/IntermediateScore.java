package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import javax.annotation.Nonnull;

public interface IntermediateScore {

    /**
     * @param miniBoard MiniBoard to score
     * @return Heuristic score evaluating the GameState from the agent's perspective, from 0 (least favorable) to 1 (most favorable)
     */
    @Nonnull Double getScore(MiniBoard miniBoard, Toml constants);

    /**
     * @return The relative weight of the scoring function
     */
    @Nonnull Double getWeight(Toml constants);
}
