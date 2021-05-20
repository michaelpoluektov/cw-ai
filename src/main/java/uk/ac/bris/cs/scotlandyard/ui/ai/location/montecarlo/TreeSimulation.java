package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TreeSimulation implements LocationPicker {
    private final Integer notifyFrequency;
    private final AbstractNode rootNode;
    private final Set<PlayoutObserver> observers = new HashSet<>();
    private final Integer coreNumber;
    private final ReentrantLock lock = new ReentrantLock();
    private final IntermediateScore[] intermediateScores;
    protected TreeSimulation(Toml constants,
                             AbstractNode rootNode,
                             Integer coreNumber,
                             IntermediateScore... intermediateScores) {
        this.notifyFrequency = constants.getLong("monteCarlo.notificationFrequency").intValue();
        this.rootNode = rootNode;
        this.coreNumber = coreNumber;
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
    public Map<Integer, Double> getScoredMap(ImmutableSet<Integer> destinations,
                                             Pair<Long, TimeUnit> simulationTime) {
        addDestinations(destinations);
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        long endTime = System.currentTimeMillis()+simulationTime.right().toMillis(simulationTime.left());
        long currentTime = System.currentTimeMillis();
        int simulations = 0;
        final ExecutorService executor = new ThreadPoolExecutor(1,
                coreNumber,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(coreNumber),
                new ThreadPoolExecutor.CallerRunsPolicy());
        RunnableSimulation runnableSimulation = new RunnableSimulation();
        while(currentTime < endTime) {
            executor.execute(runnableSimulation);
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
        System.out.println("Ran " + simulations + " simulations");
        return scoredDestinations;
    }

    private class RunnableSimulation implements Runnable {
        @Override
        public void run() {
            AbstractNode selectedNode;
            lock.lock();
            try {
                selectedNode = rootNode.select();
                selectedNode.expand();
                if(!selectedNode.getChildren().isEmpty()) {
                    selectedNode = selectedNode.getChildren()
                            .asList()
                            .get(new Random().nextInt(selectedNode.getChildren().size()));
                }
                selectedNode.backPropagatePlays();
            } finally {
                lock.unlock();
            }
            Integer rolloutResult = selectedNode.rollout(intermediateScores);
            selectedNode.backPropagateScore(rolloutResult, rootNode.getRound());
        }
    }
}
