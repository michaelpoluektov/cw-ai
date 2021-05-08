package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import javax.annotation.Nonnull;

public interface IntermediateScore {

    @Nonnull Double getScore();
    /*
    @param: MiniBoard to score

    Return: Score evaluating the GameState from the agent's perspective, from 0 (least favorable) to 1 (most favorable)
    */

    @Nonnull Double getWeight();
    /*
    @param: Toml config file

    Return: Weight of scoring function from Toml config
    */
}
