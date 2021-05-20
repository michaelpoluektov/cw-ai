package uk.ac.bris.cs.scotlandyard.ui.ai.location;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface LocationPicker {
    /**
     * @param simulationTime Maximum time to be used to return a result
     * @param destinations ImmutableSet of all available destinations
     * @return {@link Map.Entry} with best destination as key and it's score as value
     */
    @Nonnull
    Map<Integer, Double> getScoredMap(ImmutableSet<Integer> destinations,
                                      Pair<Long, TimeUnit> simulationTime);
}
