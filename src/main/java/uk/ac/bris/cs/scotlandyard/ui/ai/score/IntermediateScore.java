package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import javax.annotation.Nonnull;

public interface IntermediateScore {

    @Nonnull Double getScore(MiniBoard miniBoard);
    // @param: MiniBoard to score
    //
    // Return: Score evaluating the GameState from the agent's perspective, from 0 (least favorable) to 1 (most favorable)

    @Nonnull Double getWeight(MiniBoard miniBoard);
    // @param: Toml config file
    //
    // Return: Weight of scoring function from Toml config
}
