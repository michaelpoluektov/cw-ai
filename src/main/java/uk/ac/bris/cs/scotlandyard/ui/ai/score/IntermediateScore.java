package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import javax.annotation.Nonnull;

public interface IntermediateScore {
    /*
    @param: GameState or attributes of that GameState to score

    Return: Score evaluating the GameState from the agent's perspective, from 0 (least favorable) to 1 (most favorable)
    */
    @Nonnull Double getScore();

    @Nonnull Double getWeight();
}
