package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import javax.annotation.Nonnull;
import java.util.List;

public class StateScore {

    @Nonnull
    public static Double getTotalScore(List<IntermediateScore> intermediateScores) {
        double totalScore = 0.0;
        double totalWeights = 0.0;
        for(IntermediateScore score : intermediateScores) {
            totalScore += score.getScore()* score.getWeight();
            totalWeights += score.getWeight();
        }
        if(totalWeights == 0.0) throw new ArithmeticException("getTotalScore: All weights are zero");
        return totalScore/totalWeights;
    }
}
