package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;

import java.util.HashSet;
import java.util.Set;


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
