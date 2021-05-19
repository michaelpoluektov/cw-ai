package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TreeSimulation<T extends AbstractNode> implements LocationPicker {
    private final Integer notifyFrequency;
    private final T rootNode;
    private final Set<PlayoutObserver> observers = new HashSet<>();
    private final IntermediateScore[] intermediateScores;
    protected TreeSimulation(Toml constants, T rootNode, IntermediateScore... intermediateScores) {
        this.notifyFrequency = constants.getLong("", 100L).intValue();
        this.rootNode = rootNode;
        this.intermediateScores = intermediateScores;
    }

    public void addPlayoutObserver(PlayoutObserver observer) {
        observers.add(observer);
    }

    public void removePlayoutObserver(PlayoutObserver observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(Integer simulations, Double bestScore, Long remainingTime) {
        for(PlayoutObserver observer : observers) {
            observer.respondToPlayout(simulations, bestScore, remainingTime, this);
        }
    }

    public void addDestinations(ImmutableSet<Integer> destinations) {
        rootNode.addChildren(destinations);
    }

    @Nonnull
    @Override
    public Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations,
                                                         Pair<Long, TimeUnit> simulationTime) {
        addDestinations(destinations);
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        long endTime = System.currentTimeMillis()+simulationTime.right().toMillis(simulationTime.left());
        long currentTime = System.currentTimeMillis();
        int simulations = 0;
        while(currentTime < endTime) {
            AbstractNode selectedNode = rootNode.select();
            selectedNode.expand();
            if(!selectedNode.getChildren().isEmpty()) {
                selectedNode = selectedNode.getChildren()
                        .asList()
                        .get(new Random().nextInt(selectedNode.getChildren().size()));
            }
            Integer result = selectedNode.rollout(intermediateScores);
            selectedNode.backPropagate(result, rootNode.getRound());
            simulations++;
            currentTime = System.currentTimeMillis();
            if(simulations % notifyFrequency == 0) {
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
}
