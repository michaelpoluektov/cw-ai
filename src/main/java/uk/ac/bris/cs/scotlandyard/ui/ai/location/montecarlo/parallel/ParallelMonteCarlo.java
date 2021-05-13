package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractMonteCarlo;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.RootNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

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
    @Nonnull
    @Override
    public Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations,
                                                         Pair<Long, TimeUnit> simulationTime) {
        rootNode.addChildren(destinations);
        final HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        final long endTime = System.currentTimeMillis()+simulationTime.right().toMillis(simulationTime.left());
        long currentTime = System.currentTimeMillis();
        final ExecutorService executor = new ThreadPoolExecutor(1,
                coreNumber,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.AbortPolicy());
        //final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final ReentrantLock lock = new ReentrantLock();
        for(int i = 0; i<20000; i++) {
        //while(currentTime < endTime) {
            //rootNode.runSingleSimulation();
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
                Integer rolloutResult = selectedNode.rollout();
                selectedNode.backPropagateScore(rolloutResult, rootNode.getRound());
            });
            //currentTime = System.currentTimeMillis();
            /*if(simulations % explorationFrequency == 0) {
                Double maxScore = Collections.max(rootNode.getChildren(),
                        (Comparator.comparingDouble(AbstractNode::getAverageScore))).getAverageScore();
                notifyObservers(simulations, maxScore, endTime-currentTime);
            }*/
        }
        while(currentTime < endTime) {
            currentTime = System.currentTimeMillis();
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