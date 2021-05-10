package uk.ac.bris.cs.scotlandyard.ui.ai.location;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MiniMax implements LocationPicker{
    private final Board board;
    private final Toml constants;
    private final Integer steps;
    private final IntermediateScore[] intermediateScores;
    public MiniMax(Board board, Toml constants, Integer steps, IntermediateScore... intermediateScores) {
        this.board = board;
        this.constants = constants;
        this.steps = steps;
        this.intermediateScores = intermediateScores;
    }
    @Nonnull
    @Override
    public Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations) {
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        MiniBoard miniBoard = new MiniBoard(board, constants);
        System.out.print("Rating destinations: ");
        destinations.parallelStream()
                .forEach(destination -> addRankedMove(scoredDestinations, miniBoard, destination));
        Map.Entry<Integer, Double> bestEntry =
                Collections.max(scoredDestinations.entrySet(), Map.Entry.comparingByValue());
        System.out.println("\nBest destination is " + bestEntry.getKey() + " with score " + bestEntry.getValue());
        return bestEntry;
    }

    private void addRankedMove(HashMap<Integer, Double> map, MiniBoard miniBoard, Integer destination) {
        Double score = runMiniMax(miniBoard.advanceMrX(destination),
                steps,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        System.out.print(destination + " [" + String.format("%.2f", score) + "] ");
        map.put(destination, score);
    }

    private Double runMiniMax(MiniBoard miniBoard, Integer depth, Double alpha, Double beta){
        MiniBoard.winner winner = miniBoard.getWinner();
        if(winner == MiniBoard.winner.DETECTIVES) return 0.0;
        if(winner == MiniBoard.winner.MRX) return 1.0;
        if(depth == 0) return miniBoard.getMrXBoardScore(constants, intermediateScores);
        Boolean mrXToMove = miniBoard.getMrXToMove();
        if(mrXToMove){
            double maxScore = Double.NEGATIVE_INFINITY;
            for(Integer destination : miniBoard.getNodeDestinations(miniBoard.getMrXLocation())) {
                Double advancedScore = runMiniMax(miniBoard.advanceMrX(destination), depth - 1, alpha, beta);
                if(maxScore < advancedScore) maxScore = advancedScore;
                alpha = Double.max(alpha, advancedScore);
                if(beta <= alpha) break;
            }
            return maxScore;
        }
        else{
            double minScore = Double.POSITIVE_INFINITY;
            Integer source = miniBoard.getDetectiveLocations().get(miniBoard.getUnmovedDetectiveLocations().size()-1);
            for(Integer destination : miniBoard.getNodeDestinations(source)) {
                Double advancedScore = runMiniMax(miniBoard.advanceDetective(source, destination),
                        depth - 1,
                        alpha,
                        beta);
                if(minScore > advancedScore) minScore = advancedScore;
                beta = Double.min(beta, advancedScore);
                if(beta <= alpha) break;
            }
            return minScore;
        }
    }
}
