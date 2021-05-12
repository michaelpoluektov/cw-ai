package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

public interface PlayoutObserver {
    /**
     * Method called by a {@link MonteCarlo} object (Observable) to notify it's observer of the current predicted
     * results.
     * @param simulations Amount of simulations ran to produce the result
     * @param bestScore Best predicted score of the children {@link Node}s (representing game states)
     * @param remainingTime Remaining time (in milliseconds) until a move has to be returned
     * @param observable Reference to the observable object
     */
    void respondToPlayout(Integer simulations, Double bestScore, Long remainingTime, MonteCarlo observable);
}
