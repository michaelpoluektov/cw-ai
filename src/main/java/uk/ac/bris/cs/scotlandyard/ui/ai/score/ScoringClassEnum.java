package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXAvailableMovesScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXLocationScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXTicketScore;

public enum ScoringClassEnum {
    MRXAVAILABLEMOVES,
    MRXLOCATION,
    MRXTICKET;

    public IntermediateScore getScoringObject(MiniBoard miniBoard) {
        switch(this) {
            case MRXAVAILABLEMOVES: return new MrXAvailableMovesScore(miniBoard);
            case MRXLOCATION: return new MrXLocationScore(miniBoard);
            //case MRXTICKET: return new MrXTicketScore(miniBoard);
        }
        throw new IllegalArgumentException("No constructor available for:" + this);
    }
}