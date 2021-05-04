package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public class MrXWinnerScore implements IntermediateScore {
    private final MiniBoard.winner winner;
    public MrXWinnerScore(MiniBoard miniBoard) {
        this.winner = miniBoard.getWinner();
    }
    @Nonnull
    @Override
    public Double getScore() {
        switch(winner) {
            case MRX: return 1.0;
            case DETECTIVES: return 0.0;
            default: return 0.5;
        }
    }

    @Nonnull
    @Override
    public Double getWeight() {
        return 1.0;
    }
}
