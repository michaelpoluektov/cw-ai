package uk.ac.bris.cs.scotlandyard.ui.ai.location;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LocationPicker {
    @Nonnull Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations);
}
