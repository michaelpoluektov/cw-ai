package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import com.sun.source.tree.Tree;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MonteCarlo implements LocationPicker {
    private final Board board;
    private final Toml constants;
    private final Long endTime;
    private final Long totalTime;
    private final IntermediateScore[] intermediateScores;
    public MonteCarlo(Board board,
                      Toml constants,
                      Pair<Long, TimeUnit> timeoutPair,
                      IntermediateScore... intermediateScores) {
        this.board = board;
        this.constants = constants;
        this.endTime = System.currentTimeMillis()+timeoutPair.right().toMillis(timeoutPair.left());
        this.intermediateScores = intermediateScores;
        this.totalTime = timeoutPair.left();
    }
    @Nonnull
    @Override
    public Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations) {
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        long currentTime = System.currentTimeMillis();
        int simulations = 0;
        MCTree tree = new MCTree(board, constants);
        while(currentTime < endTime - 300) {
            if(currentTime < totalTime/3){
                if(Collections.max(scoreDestination(tree).entrySet(), Map.Entry.comparingByValue()).getValue() < 0.4){
                    tree.expandDoubleChildren();
                }
            }
            tree.runSimulation();
            simulations++;
            currentTime = System.currentTimeMillis();
        }
        scoredDestinations = scoreDestination(tree);
        Map.Entry<Integer, Double> bestEntry =
                Collections.max(scoredDestinations.entrySet(), Map.Entry.comparingByValue());
        System.out.println("Ran "
                + simulations
                + " simulations, best destination is: "
                + bestEntry.getKey()
                + " with score "
                + bestEntry.getValue());
        return bestEntry;
    }
    public HashMap<Integer, Double> scoreDestination(MCTree tree){
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        for(MCNode child : tree.getRootNode().getChildren()){
            scoredDestinations.put(child.getMiniBoard().getMrXLocation(), child.getAverageScore());
        }
        return scoredDestinations;

    }
}
