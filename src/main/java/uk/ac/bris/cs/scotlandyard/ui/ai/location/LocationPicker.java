package uk.ac.bris.cs.scotlandyard.ui.ai.location;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LocationPicker {
    @Nonnull Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations);
    // @param: ImmutableSet of all available destinations
    //
    // Return: Map.Entry with best destination as key and it's score as value
}
