package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public class MrXWinnerScore implements IntermediateScore {
    private static MrXWinnerScore instance;
    private MrXWinnerScore() {}
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard) {
        switch(miniBoard.getWinner()) {
            case MRX: return 1.0;
            case DETECTIVES: return 0.0;
            default: return 0.5;
        }
    }

    @Nonnull
    @Override
    public Double getWeight(MiniBoard miniBoard) {
        return 1.0;
    }

    public static MrXWinnerScore getInstance() {
        if(instance == null) instance = new MrXWinnerScore();
        return instance;
    }
}
