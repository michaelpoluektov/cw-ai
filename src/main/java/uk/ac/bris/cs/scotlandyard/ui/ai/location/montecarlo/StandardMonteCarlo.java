package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The standard Monte Carlo uses random moves in stage 3) for the MCTS algorithm.
 * @see AbstractMonteCarlo for details of the algorithm.
 */

public class StandardMonteCarlo extends AbstractMonteCarlo implements LocationPicker {
    private final Integer explorationFrequency;
    private final StandardRootNode rootNode;
    public StandardMonteCarlo(Board board,
                              Toml constants) {
        this.rootNode = new StandardRootNode(board, constants);
        this.explorationFrequency = constants.getLong("monteCarlo.explorationFrequency", (long) 100).intValue();
    }

    /**
     * Responsible for simulating the decision tree. While the current time is less than the end time we will continue
     * simulation.
     * <p>- The explorationFrequency is used to notify the {@link uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker}
     * observer every {@link #explorationFrequency} simulations.</p>
     * @param destinations ImmutableSet of all available destinations
     * @param simulationTime Maximum time to be used to return a result
     * @see AbstractMonteCarlo
     */
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

    @Override
    public void addDestinations(ImmutableSet<Integer> destinations) {
        rootNode.addChildren(destinations);
    }
}
