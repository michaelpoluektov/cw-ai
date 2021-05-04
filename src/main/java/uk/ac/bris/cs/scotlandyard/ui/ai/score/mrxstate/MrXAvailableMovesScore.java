package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public class MrXAvailableMovesScore implements IntermediateScore {
    private final MiniBoard miniBoard;
    private final Double availableMovesExp;
    private final Double availableMovesWeight;
    public MrXAvailableMovesScore(MiniBoard miniBoard) {
        this.miniBoard = miniBoard;
        this.availableMovesExp = miniBoard.getConstants().getDouble("availableMoves.exp");
        this.availableMovesWeight = miniBoard.getConstants().getDouble("availableMoves.weight");
    }

    @Nonnull
    @Override
    public Double getScore() {
        return 1 - Math.pow(availableMovesExp, -miniBoard.getNodeDestinations(miniBoard.getMrXLocation()).size());
    }

    @Nonnull
    @Override
    public Double getWeight() {
        return availableMovesWeight;
    }
}
