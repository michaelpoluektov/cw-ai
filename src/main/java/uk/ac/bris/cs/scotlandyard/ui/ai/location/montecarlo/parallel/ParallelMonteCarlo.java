package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractMonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used for the "smart" Monte Carlo algorithm which is our latest AI implementation. It uses concurrent
 * computing the threads to maximise the number of simulations of the tree.
 * Since the path a simulation takes down the tree is dependant on other simulations and the results they yield (number
 * of times we have played a node), the plays are backpropagated before allowing another thread to run a concurrent
 * simulation. This justifies the separation of the backpropagation of plays and score.
 * @see AbstractNode
 */


public class ParallelMonteCarlo extends AbstractMonteCarlo implements LocationPicker {
    private final IntermediateScore[] intermediateScores;
    private final Integer explorationFrequency;
    private final ParallelRootNode rootNode;
    private final Integer coreNumber = Runtime.getRuntime().availableProcessors();
    private final ReentrantLock lock = new ReentrantLock();
    public ParallelMonteCarlo(Board board,
                              Toml constants,
                              IntermediateScore... intermediateScores) {
        this.rootNode = new ParallelRootNode(board, constants);
        this.intermediateScores = intermediateScores;
        this.explorationFrequency = constants.getLong("monteCarlo.explorationFrequency", (long) 100).intValue();
    }

    /**
     * The method uses a {@link ThreadPoolExecutor} with the maximum available number of cores. It manages access to
     * those threads with a very short queue and a {@link ThreadPoolExecutor.CallerRunsPolicy} which, which prevents the
     * queue from overfilling and the method not returning in time.
     *
     * @param simulationTime Maximum time to be used to return a result, it is recommended to allow for a delay of at
     *                       least 1 second.
     */
    @Nonnull
    @Override
    public Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations,
                                                         Pair<Long, TimeUnit> simulationTime) {
        addDestinations(destinations);
        final HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        final long endTime = System.currentTimeMillis()+simulationTime.right().toMillis(simulationTime.left());
        long currentTime = System.currentTimeMillis();
        final ExecutorService executor = new ThreadPoolExecutor(1,
                coreNumber,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(coreNumber),
                new ThreadPoolExecutor.CallerRunsPolicy());
        RunnableSimulation runnableSimulation = new RunnableSimulation();
        while(currentTime < endTime) {
            executor.execute(runnableSimulation);
            currentTime = System.currentTimeMillis();
            if(rootNode.getPlays() % explorationFrequency == 0) {
                Double maxScore = Collections.max(rootNode.getChildren(),
                        (Comparator.comparingDouble(AbstractNode::getAverageScore))).getAverageScore();
                notifyObservers(rootNode.getPlays(), maxScore, endTime-currentTime);
            }
        }
        executor.shutdown();
        for(AbstractNode child : rootNode.getChildren()){
            scoredDestinations.put(child.getMiniBoard().getMrXLocation(), child.getAverageScore());
        }
        Map.Entry<Integer, Double> bestEntry =
                Collections.max(scoredDestinations.entrySet(), Map.Entry.comparingByValue());
        System.out.println("Ran "
                + rootNode.getPlays()
                + " simulations, best destination is: "
                + bestEntry.getKey()
                + " with score "
                + bestEntry.getValue());
        return bestEntry;
    }

    /**
     * Runs a single iteration of the MCTS algorithm.
     *
     * In order to manage access to potentially non thread safe variables, we use a {@link ReentrantLock}
     * which prevents threads from simultaneously altering those variables. This should not have any considerable impact
     * on performance, since the most time intensive operation to run is outside of the lock.
     */
    private class RunnableSimulation implements Runnable {
        @Override
        public void run() {
            ParallelNode selectedNode;
            lock.lock();
            try {
                selectedNode = rootNode.selectNode();
                selectedNode.expand();
                if(!selectedNode.getChildren().isEmpty()) {
                    selectedNode = (ParallelNode) selectedNode.getChildren().asList().get(0);
                } else {
                    MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
                    if(winner == MiniBoard.winner.NONE) {
                        throw new RuntimeException("State with no children has no winner!");
                    }
                }
                selectedNode.backPropagatePlays();
            } finally {
                lock.unlock();
            }
            Integer rolloutResult = selectedNode.rollout(intermediateScores);
            selectedNode.backPropagateScore(rolloutResult, rootNode.getRound());
        }
    }

    @Override public void addDestinations(ImmutableSet<Integer> destinations) {
        rootNode.addChildren(destinations);
    }
}