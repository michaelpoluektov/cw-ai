package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableMap;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;

import javax.annotation.Nonnull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScore {
    /*
    @param: GameState or attributes of that GameState to score

    Return: Score evaluating the GameState from the agent's perspective, from 0 (least favorable) to 1 (most favorable)
    */
    private final InputStream inputStream = getClass().getResourceAsStream("/constants.toml");
    protected final Toml constants = new Toml().read(inputStream);

    @Nonnull public abstract Double getMrXScore();

    @Nonnull public abstract Double getDetectivesScore();
}
