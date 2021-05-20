package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is a generic Monte Carlo (MCTS for short) class which is used to score location. The current prediction
 * for the best result can be observed when this class notifies the observers, it passes the currently best single move
 * destination from the simulation.
 *
 * <p>MCTS is a reinforcement learning algorithm well suited for sampling a game tree with a large branching
 * factor. Below you can find a quick (and rather incomplete) description of the algorithm. If you are not familiar with
 * Monte Carlo tree search, check out the <a href=https://en.wikipedia.org/wiki/Monte_Carlo_tree_search>Wikipedia</a>
 * article for more information.</p>
 *
 * In short, these four steps are run in a loop until the time limit is reached:
 *
 * <ul>
 *     <li>Selection: Go down the tree by picking the node maximising the UCT function provided by
 *     {@link NodeUCTComparator} until a leaf node is reached</li>
 *     <li>Expansion: The selected node is expanded (see {@link AbstractNode#expand()}), by adding child nodes wrapping the games states created
 *     as a result of it's available moves being played. This step is skipped if we have reached a terminal node.</li>
 *     <li>Rollout (aka playout or simulation): moves are played (either randomly by optimising a certain heuristic) until
 *     the end of the game is reached. See {@link LightNode#rollout(IntermediateScore...)} for the most commonly used implementation.</li>
 *     <li>Backpropagation: A result representing the outcome simulated by the rollout phase is then propagated back to the node's ancestors.
 *     See {@link AbstractNode#backPropagatePlays()}</li>
 * </ul>
 *
 * <p>We attempt to take full advantage of multithreading by implementing our own version of tree parallelisation, with a
 * lock placed on critical steps requiring access to and modification of globally available data.</p>
 *
 * @see MCTSMovePicker
 * @see AbstractNode
 */
public class TreeSimulation implements LocationPicker {
    private final Integer notifyFrequency;
    private final AbstractNode rootNode;
    private final Set<PlayoutObserver> observers = new HashSet<>();
    private final Integer threadNumber;
    private final AtomicInteger simulations = new AtomicInteger(); // Atomic in anticipation of full concurrency
    private final ReentrantLock lock = new ReentrantLock();
    private final NodeUCTComparator comparator;
    private final IntermediateScore[] intermediateScores;
    private long endTime; // end time is reassigned on each call to getScoredMap

    /**
     * Protected constructor to be used by a factory.
     * @param constants Toml file with constants
     * @param rootNode AbstractNode with no parent or children. The type of this node also defines the type of the tree,
     *                 since each child added to this node will have the same type.
     * @param threadNumber The amount of threads to be used. Use {@link Runtime#getRuntime()} to get the number of
     *                     available processors to take full advantage of your hardware.
     * @param intermediateScores Heuristic scoring functions potentially used by the rollout phase.
     */
    protected TreeSimulation(Toml constants,
                             AbstractNode rootNode,
                             Integer threadNumber,
                             NodeUCTComparator comparator,
                             IntermediateScore... intermediateScores) {
        this.notifyFrequency = constants.getLong("monteCarlo.notificationFrequency").intValue();
        this.rootNode = rootNode;
        this.threadNumber = threadNumber;
        this.comparator = comparator;
        this.intermediateScores = intermediateScores;
    }

    /**
     * @param observer Observer to be added
     */
    public void addPlayoutObserver(PlayoutObserver observer) {
        observers.add(observer);
    }

    /**
     * @param observer Observed to be removed
     */
    public void removePlayoutObserver(PlayoutObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of the current state of the simulation.
     * @param simulations Amount of simulations ran upon notification.
     * @param bestScore Score attributed to the move currently considered to be the most optimal.
     * @param remainingTime The remaining time until a location has to be picked.
     */
    protected void notifyObservers(Integer simulations, Double bestScore, Long remainingTime) {
        for(PlayoutObserver observer : observers) {
            observer.respondToPlayout(simulations, bestScore, remainingTime, this);
        }
    }

    public void addDestinations(ImmutableSet<Integer> destinations) {
        rootNode.addChildren(destinations);
    }

    /**
     * Samples a game tree using the MCTS method and returns a map of destinations and corresponding average scores once
     * the given simulation time runs out.
     * @param destinations ImmutableSet of all available destinations
     * @param simulationTime Maximum time to be used to return a result
     */
    @Nonnull
    @Override
    public Map<Integer, Double> getScoredMap(ImmutableSet<Integer> destinations,
                                             Pair<Long, TimeUnit> simulationTime) {
        addDestinations(destinations);
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        endTime = System.currentTimeMillis()+simulationTime.right().toMillis(simulationTime.left());
        long currentTime = System.currentTimeMillis();
        final ExecutorService executor = new ThreadPoolExecutor(1,
                threadNumber,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(threadNumber),
                new ThreadPoolExecutor.DiscardPolicy());
        RunnableSimulation runnableSimulation = new RunnableSimulation();
        while(currentTime < endTime) {
            executor.execute(runnableSimulation);
            currentTime = System.currentTimeMillis();
        }
        executor.shutdown();
        for(AbstractNode child : rootNode.getChildren()){
            scoredDestinations.put(child.getMiniBoard().getMrXLocation(), child.getAverageScore());
        }
        System.out.println("Ran " + simulations + " simulations");
        return scoredDestinations;
    }

    private AbstractNode select() {
        AbstractNode selectedNode = rootNode;
        while(true) {
            if(selectedNode.isLeaf()) return selectedNode;
            else selectedNode = selectedNode.getChildren()
                    .stream()
                    .max(comparator)
                    .orElse(selectedNode.getParent().orElse(selectedNode));
        }
    }

    /**
     * Runnable class represents task to be performed by each thread.
     * @see Runnable
     */
    private class RunnableSimulation implements Runnable {

         // Performs all 4 steps of the MCTS algorithm. Currently locks all steps to only be performed by a single
         // thread except for the rollout & backpropagate score steps. Backpropagation is split into plays and score to
         // discourage multiple threads from rolling out a single node.
        @Override
        public void run() {
            AbstractNode selectedNode;
            lock.lock();
            try {
                selectedNode = select();
                selectedNode.expand();
                if(!selectedNode.getChildren().isEmpty()) {
                    selectedNode = selectedNode.getChildren()
                            .asList()
                            .get(new Random().nextInt(selectedNode.getChildren().size()));
                }
                selectedNode.backPropagatePlays();
                // I know, it's useless for it to be atomic right now but once we add full parallelisation...
                simulations.set(simulations.get() + 1);
                // Notify observers every notifyFrequency simulations
                if(simulations.get() % notifyFrequency == 0) {
                    Double maxScore = Collections.max(rootNode.getChildren(),
                            (Comparator.comparingDouble(AbstractNode::getAverageScore))).getAverageScore();
                    notifyObservers(simulations.get(), maxScore, endTime-System.currentTimeMillis());
                }
            } finally {
                lock.unlock();
            }
            Integer rolloutResult = selectedNode.rollout(intermediateScores);
            selectedNode.backPropagateScore(rolloutResult, rootNode.getRound());
        }
    }
}
