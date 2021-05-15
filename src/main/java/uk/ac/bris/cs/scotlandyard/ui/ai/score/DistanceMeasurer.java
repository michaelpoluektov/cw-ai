package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface DistanceMeasurer {
    /**
     * Can also be used to implement detectives later
     * @param sources MrX or Detectives
     * @param destinations Detectives or possible MrX locations
     * @return List of lists containing distances from sources to destinations
     */
    public List<List<Integer>> getDistances(ImmutableList<Integer> sources,
                                            ImmutableList<Integer> destinations);
}
