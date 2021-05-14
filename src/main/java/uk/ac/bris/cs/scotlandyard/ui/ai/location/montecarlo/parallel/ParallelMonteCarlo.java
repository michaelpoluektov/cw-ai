package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractMonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.standard.StandardNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used for the "smart" Monte Carlo algorithm which is the final Ai we have implemented. It uses
 * concepts from concurrent computing and parallelises the threads to maximise the number of simulations of the tree.
 * Since the path a simulation takes down the tree is dependant on other simulations and the results they yield (number
 * of times we have played a node), we needed to backpropagate the number of plays before allowing another thread
 * to run a concurrent simulation. This was the reason back propagate plays and back propagate score were separated in
 * {@link AbstractNode}.
 */

public class ParallelMonteCarlo extends AbstractMonteCarlo implements LocationPicker {
    private final IntermediateScore[] intermediateScores;
    private final Integer explorationFrequency;
    private final ParallelRootNode rootNode;
    private final Integer coreNumber = Runtime.getRuntime().availableProcessors();
    public ParallelMonteCarlo(Board board,
                              Toml constants,
                              IntermediateScore... intermediateScores) {
        this.rootNode = new ParallelRootNode(board, constants);
        this.intermediateScores = intermediateScores;
        this.explorationFrequency = constants.getLong("monteCarlo.explorationFrequency", (long) 100).intValue();
    }

    /**
     * We instantiate a new {@link ThreadPoolExecutor} which takes as parameter the number of cores the pc running the programme has. This
     * dictates the maximum number of threads we should have running at one time. The {@link ArrayBlockingQueue} refers to the
     * maximum number of runnables the queue can store. If we try to execute a runnable and there are no threads
     * available in the {@link ThreadPoolExecutor} and the queue is full, then the CallerRunsPolicy is called (Last parameter).
     * This uses the thread responsible for the execution of the while loop to execute this runnable therefore preventing any
     * other runnable from entering the while loop.
     *
     * <p>- In order to concurrently run threads we use the {@link ReentrantLock} which prevents threads altering
     * variables that other threads will use at the same time. The first thread that reaches the while loop locks the
     * lambda function until it has back propagated the plays, at which point it unlocks the lambda function allowing the
     * next thread to enter. Once the current time exceeds the end tme we shutdiwn the executor and select the best child
     * node</p>
     *
     * @param destinations ImmutableSet of all available destinations
     * @param simulationTime Maximum time to be used to return a result
     * @return
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
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.CallerRunsPolicy());
        final ReentrantLock lock = new ReentrantLock();
        while(currentTime < endTime) {
            executor.execute(() -> {
                ParallelNode selectedNode;
                lock.lock();
                try {
                    selectedNode = rootNode.selectNode();
                    selectedNode.expand();
                    if(!selectedNode.getChildren().isEmpty()) {
                        selectedNode = (ParallelNode) selectedNode.getChildren().asList().get(0);
                    } else {
                        MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
                        if(winner == MiniBoard.winner.NONE) throw new RuntimeException("State with no children has no winner!");
                    }
                    selectedNode.backPropagatePlays();
                } finally {
                    lock.unlock();
                }
                Integer rolloutResult = selectedNode.rollout(intermediateScores);
                selectedNode.backPropagateScore(rolloutResult, rootNode.getRound());

            });
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

    @Override public void addDestinations(ImmutableSet<Integer> destinations) {
        rootNode.addChildren(destinations);
    }
}