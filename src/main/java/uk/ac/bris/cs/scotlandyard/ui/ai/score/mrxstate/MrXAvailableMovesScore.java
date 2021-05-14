package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

/**
 * @see IntermediateScore
 * Implementation for the class responsible for scoring a MrX state based on the number of available moves he will have.
 */
public class MrXAvailableMovesScore implements IntermediateScore {
    private final Double availableMovesExp;
    private final Double availableMovesWeight;
    public MrXAvailableMovesScore(Toml constants) {
        this.availableMovesExp = constants.getDouble("availableMoves.exp");
        this.availableMovesWeight = constants.getDouble("availableMoves.weight");
    }

    /**
     * Uses an exponential function we selected to score a {@link MiniBoard} based on moves. The exponent is stored in the {@link Toml} file
     * @param miniBoard MiniBoard to score. We use this to find out how many available moves this {@link MiniBoard}
     *                 will lead to.
     */
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard) {
        return 1 - Math.pow(availableMovesExp, -miniBoard.getNodeDestinations(miniBoard.getMrXLocation()).size());
    }

    @Nonnull
    @Override
    public Double getWeight() {
        return availableMovesWeight;
    }

}
