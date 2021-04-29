package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MrXAvailableMovesScore implements IntermediateScore {
    private final Board board;
    private final Double availableMovesExp;
    private final Double availableMovesWeight;
    public MrXAvailableMovesScore(Toml constants, Board board) {
        this.board = board;
        this.availableMovesExp = constants.getDouble("availablemoves.exp");
        this.availableMovesWeight = constants.getDouble("availablemoves.weight");
    }

    @Nonnull
    @Override
    public Double getScore() {
        List<Move> availableSingleMoves = new ArrayList<>();
        for(Move move : board.getAvailableMoves()) {
            if(move.getClass() == Move.SingleMove.class) availableSingleMoves.add(move); //should this add only single moves?
        }
        double score = 1 - Math.pow(availableMovesExp, -availableSingleMoves.size());
        return score;

    }
    @Nonnull
    @Override
    public Double getWeight(){
        return availableMovesWeight;
    }


}
