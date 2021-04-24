package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;

import javax.annotation.Nonnull;

public interface Score {
    /*
    @param: GameState to score

    Return: Score evaluating the GameState from the agent's perspective, from 0 (least favorable) to 1 (most favorable)
    */
    @Nonnull Double getMrXScore();

    @Nonnull Double getDetectivesScore();
}
