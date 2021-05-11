package uk.ac.bris.cs.scotlandyard.ui.ai.location;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LocationPicker {
    /**
     * @param destinations ImmutableSet of all available destinations
     *
     * @return Map.Entry with best destination as key and it's score as value
     */
    @Nonnull Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations);
}
