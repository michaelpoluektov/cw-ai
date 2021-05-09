package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public class MrXAvailableMovesScore implements IntermediateScore {
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard) {
        final Double availableMovesExp = miniBoard.getConstants().getDouble("availableMoves.exp");
        return 1 - Math.pow(availableMovesExp, -miniBoard.getNodeDestinations(miniBoard.getMrXLocation()).size());
    }

    @Nonnull
    @Override
    public Double getWeight(MiniBoard miniBoard) {
        return miniBoard.getConstants().getDouble("availableMoves.weight");
    }
}
