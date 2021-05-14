package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * This class is a generic Monte Carlo class which is used to score location. This class is observed by the
 * {@link uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker}. When this class notifies the observers, it
 * passes the currently best single move destination from the simulation.
 * See <a href = https://medium.com/@quasimik/monte-carlo-tree-search-applied-to-letterpress-34f41c86e238>Medium</a> for
 * more information on MCTS.
 * @see uk.ac.bris.cs.scotlandyard.ui.ai.mrx.movepicker.MCTSMovePicker
 */
public abstract class AbstractMonteCarlo implements LocationPicker {
    private final Set<PlayoutObserver> observers = new HashSet<>();

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

    public abstract void addDestinations(ImmutableSet<Integer> destinations);
}
