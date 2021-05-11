package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public class MrXAvailableMovesScore implements IntermediateScore {
    private final Double availableMovesExp;
    private final Double availableMovesWeight;
    public MrXAvailableMovesScore(Toml constants) {
        this.availableMovesExp = constants.getDouble("availableMoves.exp");
        this.availableMovesWeight = constants.getDouble("availableMoves.weight");
    }
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
