package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractMonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.RootNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParallelMonteCarlo extends AbstractMonteCarlo implements LocationPicker {
    private final IntermediateScore[] intermediateScores;
    private final Integer explorationFrequency;
    private final RootNode rootNode;
    public ParallelMonteCarlo(Board board,
                              Toml constants,
                              IntermediateScore... intermediateScores) {
        this.rootNode = new ParallelRootNode(board, constants);
        this.intermediateScores = intermediateScores;
        this.explorationFrequency = constants.getLong("monteCarlo.explorationFrequency", (long) 100).intValue();
    }
    @Nonnull
    @Override
    public Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations,
                                                         Pair<Long, TimeUnit> simulationTime) {
        rootNode.addChildren(destinations);
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        long endTime = System.currentTimeMillis()+simulationTime.right().toMillis(simulationTime.left());
        long currentTime = System.currentTimeMillis();
        int simulations = 0;
        while(currentTime < endTime) {
            rootNode.runSingleSimulation();
            simulations++;
            currentTime = System.currentTimeMillis();
            if(simulations % explorationFrequency == 0) {
                Double maxScore = Collections.max(rootNode.getChildren(),
                        (Comparator.comparingDouble(AbstractNode::getAverageScore))).getAverageScore();
                notifyObservers(simulations, maxScore, endTime-currentTime);
            }
        }
        for(AbstractNode child : rootNode.getChildren()){
            scoredDestinations.put(child.getMiniBoard().getMrXLocation(), child.getAverageScore());
        }
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

    @Override public void addDestinations(ImmutableSet<Integer> destinations) {
        rootNode.addChildren(destinations);
    }
}