package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is a generic Monte Carlo (MCTS for short) class which is used to score location. The current prediction
 * for the best result can be observed when this class notifies the observers, it passes the currently best single move
 * destination from the simulation.
 *
 * MCTS is a reinforcement learning algorithm well suited to adequately sample a game tree with a large branching
 * factor. See <a href=https://en.wikipedia.org/wiki/Monte_Carlo_tree_search>Wikipedia</a> for more information.
 * @see MCTSMovePicker
 * @see AbstractNode
 * @see RootNode
 */
public abstract class AbstractMonteCarlo implements LocationPicker {
    private final Set<PlayoutObserver> observers = new HashSet<>();

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

    public abstract void addDestinations(ImmutableSet<Integer> destinations);
}
