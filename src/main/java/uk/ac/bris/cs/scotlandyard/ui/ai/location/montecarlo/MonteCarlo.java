package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MonteCarlo implements LocationPicker {
    private final RootNode rootNode;
    private final IntermediateScore[] intermediateScores;
    private final Set<PlayoutObserver> observers;
    private final Integer explorationFrequency;
    public MonteCarlo(Board board,
                      Toml constants,
                      IntermediateScore... intermediateScores) {
        this.rootNode = new RootNode(board, constants);
        this.intermediateScores = intermediateScores;
        this.observers = new HashSet<>();
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
            rootNode.runSimulation();
            simulations++;
            currentTime = System.currentTimeMillis();
            if(simulations % explorationFrequency == 0) {
                Double maxScore = Collections.max(rootNode.getChildren(),
                        (Comparator.comparingDouble(Node::getAverageScore))).getAverageScore();
                notifyObservers(simulations, maxScore, endTime-currentTime);
            }
        }
        for(Node child : rootNode.getChildren()){
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

    public void addPlayoutObserver(PlayoutObserver observer) {
        observers.add(observer);
    }

    public void removePlayoutObserver(PlayoutObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Integer simulations, Double bestScore, Long remainingTime) {
        for(PlayoutObserver observer : observers) {
            observer.respondToPlayout(simulations, bestScore, remainingTime, this);
        }
    }

    public void addDestinations(ImmutableSet<Integer> destinations) {
        rootNode.addChildren(destinations);
    }
}
