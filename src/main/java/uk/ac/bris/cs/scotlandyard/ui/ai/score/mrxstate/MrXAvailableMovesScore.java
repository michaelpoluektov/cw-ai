package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public class MrXAvailableMovesScore implements IntermediateScore {
    private static MrXAvailableMovesScore instance;
    private MrXAvailableMovesScore() {}
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard, Toml constants) {
        final Double availableMovesExp = constants.getDouble("availableMoves.exp");
        return 1 - Math.pow(availableMovesExp, -miniBoard.getNodeDestinations(miniBoard.getMrXLocation()).size());
    }

    @Nonnull
    @Override
    public Double getWeight(Toml constants) {
        return constants.getDouble("availableMoves.weight");
    }

    public static MrXAvailableMovesScore getInstance() {
        if(instance == null) instance = new MrXAvailableMovesScore();
        return instance;
    }
}
